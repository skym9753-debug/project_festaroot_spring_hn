package com.study.app.domains.admin.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.admin.dao.AdminCommentDAO;
import com.study.app.domains.board.dto.CommentReportDTO;
import com.study.app.domains.board.dto.PostCommentDTO;

@Service
public class AdminCommentService {


	private static final Set<String> CATEGORIES =
			Set.of("all", "free", "review", "tip", "notice");

	private static final Set<String> COMMENT_TYPES =
			Set.of("all", "COMMENT", "REPLY");

	private static final Set<String> SEARCH_TYPES =
			Set.of(
					"content",
					"author",
					"id",
					"postTitle",
					"postId"
					);

	private static final Set<String> REPORT_RESULTS =
			Set.of("ACCEPTED", "REJECTED");

	private final AdminCommentDAO adminCommentDAO;

	public AdminCommentService(
			AdminCommentDAO adminCommentDAO
			) {
		this.adminCommentDAO = adminCommentDAO;
	}

	public Map<String, Object> getSummary() {
		return adminCommentDAO.selectSummary();
	}

	public Map<String, Object> getComments(
			int page,
			int size,
			String category,
			String commentType,
			String searchType,
			String keyword
			) {
		page = normalizePage(page);
		size = normalizeSize(size);
		category = normalizeCategory(category);
		commentType = normalizeCommentType(commentType);
		searchType = normalizeSearchType(searchType);
		keyword = keyword == null ? "" : keyword.trim();

		int startRow = (page - 1) * size + 1;
		int endRow = page * size;

		Map<String, Object> params = new HashMap<>();
		params.put("startRow", startRow);
		params.put("endRow", endRow);
		params.put("category", category);
		params.put("commentType", commentType);
		params.put("searchType", searchType);
		params.put("keyword", keyword);

		List<PostCommentDTO> rawComments =
				adminCommentDAO.selectComments(params);

		List<Map<String, Object>> comments = rawComments.stream()
				.map(this::toCommentMap)
				.toList();

		int totalCount =
				adminCommentDAO.countComments(params);

		return createPageResponse(
				comments,
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

		List<Map<String, Object>> rawRows =
				adminCommentDAO.selectWaitingReports(params);

		/*
		 * 현재 CommentManagementPage JSX의
		 * { comment, report } 구조에 맞춰 반환합니다.
		 */
		List<Map<String, Object>> list = rawRows.stream()
				.map(this::toWaitingReportRow)
				.toList();

		int totalCount =
				adminCommentDAO.countWaitingReports();

		return createPageResponse(
				list,
				page,
				size,
				totalCount
				);
	}

	public Map<String, Object> getCommentDetail(
			Long commentId
			) {
		PostCommentDTO comment =
				adminCommentDAO.selectCommentDetail(commentId);

		if (comment == null) {
			throw new IllegalArgumentException(
					"댓글을 찾을 수 없습니다."
					);
		}

		List<CommentReportDTO> reports =
				adminCommentDAO.selectReportsByCommentId(commentId);

		Map<String, Object> response =
				toCommentMap(comment);

		response.put(
				"reportItems",
				reports.stream()
				.map(this::toReportMap)
				.toList()
				);

		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public void processReport(
			Long commentId,
			Long reportId,
			String resultStatus,
			String adminMemo
			) {
		resultStatus = resultStatus == null
				? ""
						: resultStatus.trim().toUpperCase();

		if (!REPORT_RESULTS.contains(resultStatus)) {
			throw new IllegalArgumentException(
					"처리 상태는 ACCEPTED 또는 REJECTED만 가능합니다."
					);
		}

		adminMemo = adminMemo == null
				? ""
						: adminMemo.trim();

		if (adminMemo.length() > 1000) {
			throw new IllegalArgumentException(
					"관리자 메모는 1000자 이하로 입력해주세요."
					);
		}

		Map<String, Object> params = new HashMap<>();
		params.put("commentId", commentId);
		params.put("reportId", reportId);
		params.put("resultStatus", resultStatus);
		params.put("adminMemo", adminMemo);

		/*
		 * 현재 WAITING 상태인 신고 한 건만 변경합니다.
		 * 이미 다른 관리자가 처리했다면 update 결과가 0입니다.
		 */
		int updatedCount =
				adminCommentDAO.updateReportStatus(params);

		if (updatedCount == 0) {
			throw new IllegalStateException(
					"신고를 찾을 수 없거나 이미 처리된 신고입니다."
					);
		}

		/*
		 * HISTORY_ID용 시퀀스가 없으므로
		 * MAX(HISTORY_ID) + 1 충돌 방지용 잠금입니다.
		 */
		adminCommentDAO.lockHistoryTable();

		int insertedCount =
				adminCommentDAO.insertReportHistory(params);

		if (insertedCount != 1) {
			throw new IllegalStateException(
					"신고 처리 이력 저장에 실패했습니다."
					);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteComment(Long commentId) {
		deleteCommentTrees(List.of(commentId));
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteComments(
			List<Long> commentIds
			) {
		if (commentIds == null || commentIds.isEmpty()) {
			throw new IllegalArgumentException(
					"삭제할 댓글을 선택해주세요."
					);
		}

		deleteCommentTrees(commentIds);
	}

	private void deleteCommentTrees(
			List<Long> rootCommentIds
			) {
		/*
		 * 부모 댓글 삭제 시 대댓글이 고아 데이터로 남지 않도록
		 * 선택 댓글과 모든 하위 대댓글 ID를 조회합니다.
		 */
		List<Long> deleteIds =
				adminCommentDAO.selectCommentTreeIds(rootCommentIds);

		if (deleteIds == null || deleteIds.isEmpty()) {
			throw new IllegalArgumentException(
					"삭제할 댓글을 찾을 수 없습니다."
					);
		}

		adminCommentDAO.deleteCommentReportsByIds(deleteIds);
		adminCommentDAO.deleteCommentLikesByIds(deleteIds);

		int deletedCount =
				adminCommentDAO.deleteCommentsByIds(deleteIds);

		if (deletedCount == 0) {
			throw new IllegalStateException(
					"댓글 삭제에 실패했습니다."
					);
		}

		/*
		 * MEMBER_REPORT_HISTORY는 관리자 처리 감사 이력이므로
		 * 댓글이 삭제되더라도 유지합니다.
		 */
	}

	private Map<String, Object> toCommentMap(
			PostCommentDTO comment
			) {
		Map<String, Object> row = new LinkedHashMap<>();

		row.put("commentId", comment.getComment_id());
		row.put(
				"commentCode",
				createCommentCode(comment.getComment_id())
				);

		row.put("postId", comment.getPost_id());
		row.put(
				"postCode",
				createPostCode(comment.getPost_id())
				);

		row.put("postTitle", comment.getPost_title());
		row.put("postCategory", comment.getPost_category());

		row.put("memberId", comment.getMember_id());
		row.put("nickname", comment.getNickname());
		row.put(
				"profileImageUrl",
				comment.getProfile_image_url()
				);

		row.put("content", comment.getContent());
		row.put(
				"parentCommentId",
				comment.getParent_comment_id()
				);

		row.put(
				"commentType",
				comment.getParent_comment_id() == null
				? "COMMENT"
						: "REPLY"
				);

		row.put(
				"createdAt",
				comment.getCreated_at_text()
				);
		row.put(
				"updatedAt",
				comment.getUpdated_at_text()
				);

		row.put(
				"likeCount",
				valueOrZero(comment.getLike_count())
				);
		row.put(
				"reportCount",
				valueOrZero(comment.getReport_count())
				);
		row.put(
				"pendingReportCount",
				valueOrZero(comment.getPending_report_count())
				);

		return row;
	}

	private Map<String, Object> toReportMap(
			CommentReportDTO report
			) {
		Map<String, Object> row = new LinkedHashMap<>();

		row.put("reportId", report.getReport_id());
		row.put(
				"reportCode",
				createReportCode(report.getReport_id())
				);

		row.put("commentId", report.getComment_id());
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

	private Map<String, Object> toWaitingReportRow(
			Map<String, Object> raw
			) {
		Map<String, Object> comment = new LinkedHashMap<>();
		comment.put("commentId", raw.get("commentId"));
		comment.put("postId", raw.get("postId"));
		comment.put("postTitle", raw.get("postTitle"));
		comment.put("postCategory", raw.get("postCategory"));
		comment.put("memberId", raw.get("commentAuthor"));
		comment.put(
				"parentCommentId",
				raw.get("parentCommentId")
				);
		comment.put("content", raw.get("commentContent"));
		comment.put(
				"reportCount",
				raw.get("commentReportCount")
				);

		Map<String, Object> report = new LinkedHashMap<>();
		report.put("reportId", raw.get("reportId"));
		report.put(
				"reporterMemberId",
				raw.get("reporterMemberId")
				);
		report.put("reason", raw.get("reason"));
		report.put("createdAt", raw.get("createdAt"));
		report.put("status", raw.get("status"));

		Map<String, Object> row = new LinkedHashMap<>();
		row.put("comment", comment);
		row.put("report", report);

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
				(int) Math.ceil(
						(double) totalCount / size
						)
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
		String value = category == null
				? "all"
						: category.trim().toLowerCase();

		return CATEGORIES.contains(value)
				? value
						: "all";
	}

	private String normalizeCommentType(String commentType) {
		String value = commentType == null
				? "all"
						: commentType.trim().toUpperCase();

		return COMMENT_TYPES.contains(value)
				? value
						: "all";
	}

	private String normalizeSearchType(String searchType) {
		String value = searchType == null
				? "content"
						: searchType.trim();

		return SEARCH_TYPES.contains(value)
				? value
						: "content";
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

	private long valueOrZero(Long value) {
		return value == null ? 0L : value;
	}

	private String createCommentCode(Long commentId) {
		return commentId == null
				? null
						: String.format("CMT-%05d", commentId);
	}

	private String createPostCode(Long postId) {
		return postId == null
				? null
						: String.format("POST-%03d", postId);
	}

	private String createReportCode(Long reportId) {
		return reportId == null
				? null
						: String.format("RPT-%05d", reportId);
	}

}
