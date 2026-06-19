package com.study.app.domains.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import com.study.app.domains.admin.service.AdminReviewService;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;

@RestController
@RequestMapping("/admin/reviews")
public class AdminReviewController {
	
	@Autowired
	private AdminReviewService adminReviewService;

	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> getReviews(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "status", defaultValue = "ALL") String status,
			@RequestParam(value = "rating", defaultValue = "ALL") String rating,
			@RequestParam(value = "keyword", required = false) String keyword) {
		
		Map<String, Object> result = adminReviewService.getAdminReviews(page, size, status, rating, keyword);
		return ResponseEntity.ok(result);
	}

	@PatchMapping("/{reviewId}/status")
	public ResponseEntity<?> updateReviewStatus(
			@PathVariable Long reviewId,
			@RequestBody Map<String, String> body) {
		String isDeleted = body.get("is_deleted");
		boolean success = adminReviewService.updateReviewStatus(reviewId, isDeleted);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", success);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{reviewId}/reports/dismiss")
	public ResponseEntity<?> dismissReviewReports(@PathVariable Long reviewId) {
		boolean success = adminReviewService.dismissReviewReports(reviewId);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", success);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
		boolean success = adminReviewService.deleteReview(reviewId);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", success);
		return ResponseEntity.ok(response);
	}

}
