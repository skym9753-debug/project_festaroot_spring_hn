package com.study.app.domains.admin.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.admin.dao.AdminPostDAO;
import com.study.app.domains.admin.dto.AdminPostDTO;
import com.study.app.domains.board.dto.PostAttachmentDTO;
import com.study.app.domains.board.dto.PostReportDTO;

@Service
public class AdminPostService {
	
    private static final Set<String> SEARCH_TYPES =
            Set.of("title", "author", "id");

    private static final Set<String> CATEGORIES =
            Set.of("all", "free", "review", "tip", "notice");

    private static final Set<String> REPORT_RESULTS =
            Set.of("ACCEPTED", "REJECTED");

    private final AdminPostDAO adminPostDAO;

    public AdminPostService(AdminPostDAO adminPostDAO) {
        this.adminPostDAO = adminPostDAO;
    }

    public Map<String, Object> getSummary() {
        return adminPostDAO.selectSummary();
    }

    public Map<String, Object> getPosts(
            int page,
            int size,
            String category,
            String searchType,
            String keyword
    ) {
        page = normalizePage(page);
        size = normalizeSize(size);

        category = normalizeCategory(category);
        searchType = normalizeSearchType(searchType);
        keyword = keyword == null ? "" : keyword.trim();

        int startRow = (page - 1) * size + 1;
        int endRow = page * size;

        Map<String, Object> params = new HashMap<>();
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        params.put("category", category);
        params.put("searchType", searchType);
        params.put("keyword", keyword);

        List<AdminPostDTO> list = adminPostDAO.selectPosts(params);
        int totalCount = adminPostDAO.countPosts(params);

        return createPageResponse(
                list,
                page,
                size,
                totalCount
        );
    }

    public Map<String, Object> getWaitingReports(
            int page,
            int size
    ) {
        page = normalizePage(page);
        size = normalizeSize(size);

        int startRow = (page - 1) * size + 1;
        int endRow = page * size;

        Map<String, Object> params = new HashMap<>();
        params.put("startRow", startRow);
        params.put("endRow", endRow);

        List<PostReportDTO> rawList =
                adminPostDAO.selectWaitingReports(params);

        List<Map<String, Object>> list = rawList.stream()
                .map(this::toWaitingReportMap)
                .toList();

        int totalCount = adminPostDAO.countWaitingReports();

        return createPageResponse(
                list,
                page,
                size,
                totalCount
        );
    }

    public AdminPostDTO getPostDetail(Long postId) {
        AdminPostDTO post = adminPostDAO.selectPostDetail(postId);

        if (post == null) {
            throw new IllegalArgumentException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        List<PostReportDTO> rawReports =
                adminPostDAO.selectReportsByPostId(postId);

        List<PostAttachmentDTO> rawAttachments =
                adminPostDAO.selectAttachmentsByPostId(postId);

        List<Map<String, Object>> reportItems = rawReports.stream()
                .map(this::toReportItemMap)
                .toList();

        List<Map<String, Object>> attachments = rawAttachments.stream()
                .map(this::toAttachmentMap)
                .toList();

        post.setReportItems(reportItems);
        post.setAttachments(attachments);

        return post;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processReport(
            Long postId,
            Long reportId,
            String resultStatus,
            String adminMemo
    ) {
        resultStatus = resultStatus == null
                ? ""
                : resultStatus.trim().toUpperCase();

        if (!REPORT_RESULTS.contains(resultStatus)) {
            throw new IllegalArgumentException(
                    "신고 처리 상태는 ACCEPTED 또는 REJECTED만 가능합니다."
            );
        }

        adminMemo = adminMemo == null ? "" : adminMemo.trim();

        if (adminMemo.length() > 1000) {
            throw new IllegalArgumentException(
                    "관리자 메모는 1000자 이하로 입력해주세요."
            );
        }

        Map<String, Object> params = new HashMap<>();
        params.put("postId", postId);
        params.put("reportId", reportId);
        params.put("resultStatus", resultStatus);
        params.put("adminMemo", adminMemo);

        /*
         * HISTORY_ID를 MAX + 1로 생성하므로
         * 동일 시점 중복 생성을 막기 위해 history 테이블을 잠급니다.
         */
        adminPostDAO.lockHistoryTable();

        int updatedCount =
                adminPostDAO.updateReportStatus(params);

        if (updatedCount == 0) {
            throw new IllegalStateException(
                    "신고를 찾을 수 없거나 이미 처리된 신고입니다."
            );
        }

        int insertedCount =
                adminPostDAO.insertReportHistory(params);

        if (insertedCount != 1) {
            throw new IllegalStateException(
                    "신고 처리 이력 저장에 실패했습니다."
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        deleteOnePost(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePosts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "삭제할 게시글을 선택해주세요."
            );
        }

        for (Long postId : postIds) {
            deleteOnePost(postId);
        }
    }

    private void deleteOnePost(Long postId) {
        List<String> filePaths =
                adminPostDAO.selectAttachmentPathsByPostId(postId);

        adminPostDAO.deleteCommentReportsByPostId(postId);
        adminPostDAO.deleteCommentLikesByPostId(postId);
        adminPostDAO.deleteCommentsByPostId(postId);

        adminPostDAO.deletePostLikesByPostId(postId);
        adminPostDAO.deleteAttachmentsByPostId(postId);
        adminPostDAO.deletePostReportsByPostId(postId);

        int deletedCount =
                adminPostDAO.deletePostById(postId);

        if (deletedCount == 0) {
            throw new IllegalArgumentException(
                    "게시글을 찾을 수 없습니다. postId=" + postId
            );
        }

        /*
         * DB 삭제 성공 이후 기존 UploadService 메서드로
         * filePaths의 GCS/S3/로컬 파일을 삭제합니다.
         *
         * MEMBER_REPORT_HISTORY는 관리자 감사 이력이므로
         * 게시글 삭제 시에도 유지합니다.
         */
    }

    private Map<String, Object> toWaitingReportMap(
            PostReportDTO report
    ) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("reportId", report.getReport_id());
        row.put("postId", report.getPost_id());

        row.put(
                "reportCode",
                createReportCode(report.getReport_id())
        );
        row.put(
                "postCode",
                createPostCode(report.getPost_id())
        );

        row.put("title", report.getPost_title());
        row.put("category", report.getPost_category());

        row.put(
                "reporterMemberId",
                report.getMember_id()
        );
        row.put("reason", report.getReason());
        row.put(
                "createdAt",
                report.getCreated_at_text()
        );
        row.put("status", report.getStatus());

        row.put(
                "postReportCount",
                report.getPost_report_count()
        );

        return row;
    }

    private Map<String, Object> toReportItemMap(
            PostReportDTO report
    ) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("reportId", report.getReport_id());
        row.put("postId", report.getPost_id());

        row.put(
                "reportCode",
                createReportCode(report.getReport_id())
        );

        row.put(
                "reporterMemberId",
                report.getMember_id()
        );

        row.put("reason", report.getReason());
        row.put(
                "createdAt",
                report.getCreated_at_text()
        );
        row.put("status", report.getStatus());
        row.put("adminMemo", report.getAdmin_memo());
        row.put("processedAt", report.getProcessed_at());

        return row;
    }

    private Map<String, Object> toAttachmentMap(
            PostAttachmentDTO attachment
    ) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("attachId", attachment.getAttach_id());
        row.put("postId", attachment.getPost_id());
        row.put("name", attachment.getFile_name());
        row.put("path", attachment.getFile_path());
        row.put("size", attachment.getFile_size());

        String fileType = attachment.getFile_type();

        row.put(
                "type",
                "IMAGE".equalsIgnoreCase(fileType)
                        ? "image"
                        : "file"
        );

        return row;
    }

    private Map<String, Object> createPageResponse(
            Object list,
            int page,
            int size,
            int totalCount
    ) {
        int totalPages = Math.max(
                1,
                (int) Math.ceil((double) totalCount / size)
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("list", list);
        response.put("page", page);
        response.put("size", size);
        response.put("totalCount", totalCount);
        response.put("totalPages", totalPages);

        return response;
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return "all";
        }

        String normalized = category.trim().toLowerCase();

        return CATEGORIES.contains(normalized)
                ? normalized
                : "all";
    }

    private String normalizeSearchType(String searchType) {
        if (searchType == null) {
            return "title";
        }

        String normalized = searchType.trim().toLowerCase();

        return SEARCH_TYPES.contains(normalized)
                ? normalized
                : "title";
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 5;
        }

        return Math.min(size, 50);
    }

    private String createPostCode(Long postId) {
        if (postId == null) {
            return null;
        }

        return String.format("POST-%03d", postId);
    }

    private String createReportCode(Long reportId) {
        if (reportId == null) {
            return null;
        }

        return String.format("RPT-%05d", reportId);
    }

}
