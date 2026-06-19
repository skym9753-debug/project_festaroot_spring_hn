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

import com.study.app.domains.admin.service.AdminCommentService;

@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    public AdminCommentController(
            AdminCommentService adminCommentService
    ) {
        this.adminCommentService = adminCommentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(
                adminCommentService.getSummary()
        );
    }

    @GetMapping("/waiting-reports")
    public ResponseEntity<Map<String, Object>> getWaitingReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(
                adminCommentService.getWaitingReports(
                        page,
                        size
                )
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "all") String commentType,
            @RequestParam(defaultValue = "content") String searchType,
            @RequestParam(defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(
                adminCommentService.getComments(
                        page,
                        size,
                        category,
                        commentType,
                        searchType,
                        keyword
                )
        );
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> getCommentDetail(
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(
                adminCommentService.getCommentDetail(commentId)
        );
    }

    @PatchMapping("/{commentId}/reports/{reportId}")
    public ResponseEntity<Map<String, String>> processReport(
            @PathVariable Long commentId,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request
    ) {
        adminCommentService.processReport(
                commentId,
                reportId,
                request.get("resultStatus"),
                request.get("adminMemo")
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "댓글 신고 처리가 완료되었습니다."
                )
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId
    ) {
        adminCommentService.deleteComment(commentId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "댓글이 완전 삭제되었습니다."
                )
        );
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, String>> deleteComments(
            @RequestBody List<Long> commentIds
    ) {
        adminCommentService.deleteComments(commentIds);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "선택한 댓글이 완전 삭제되었습니다."
                )
        );
    }
}
