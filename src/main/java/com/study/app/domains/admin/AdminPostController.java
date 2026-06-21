package com.study.app.domains.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.dto.AdminPostDTO;
import com.study.app.domains.admin.service.AdminPostService;

@RestController
@RequestMapping("/admin/posts")
public class AdminPostController {
	
    private final AdminPostService adminPostService;

    public AdminPostController(
            AdminPostService adminPostService
    ) {
        this.adminPostService = adminPostService;
    }

    // 상단 통계
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(
                adminPostService.getSummary()
        );
    }

    // 처리 대기 신고 목록
    @GetMapping("/waiting-reports")
    public ResponseEntity<Map<String, Object>> getWaitingReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(
                adminPostService.getWaitingReports(page, size)
        );
    }

    // 전체 게시글 목록
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "title") String searchType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String visibleStatus
    ) {
        return ResponseEntity.ok(
                adminPostService.getPosts(
                        page,
                        size,
                        category,
                        searchType,
                        keyword,
                        visibleStatus
                )
        );
    }
   

    // 게시글 상세
    @GetMapping("/{postId}")
    public ResponseEntity<AdminPostDTO> getPostDetail(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                adminPostService.getPostDetail(postId)
        );
    }

    // 신고 한 건 인정/반려
    @PatchMapping("/{postId}/reports/{reportId}")
    public ResponseEntity<Map<String, String>> processReport(
            @PathVariable Long postId,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request
    ) {
        adminPostService.processReport(
                postId,
                reportId,
                request.get("resultStatus"),
                request.get("adminMemo")
        );

        return ResponseEntity.ok(
                Map.of("message", "신고 처리가 완료되었습니다.")
        );
    }

    // 게시글 한 건 완전 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long postId
    ) {
        adminPostService.deletePost(postId);

        return ResponseEntity.ok(
                Map.of("message", "게시글이 완전 삭제되었습니다.")
        );
    }

    // 게시글 선택 삭제
    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, String>> deletePosts(
            @RequestBody List<Long> postIds
    ) {
        adminPostService.deletePosts(postIds);

        return ResponseEntity.ok(
                Map.of("message", "선택한 게시글이 완전 삭제되었습니다.")
        );
    }

}
