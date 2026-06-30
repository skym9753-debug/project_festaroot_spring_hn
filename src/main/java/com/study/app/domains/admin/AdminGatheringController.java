package com.study.app.domains.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.gathering.GatheringService;

@RestController
@RequestMapping("/admin/gatherings")
public class AdminGatheringController {

	private final GatheringService gatheringService;

	public AdminGatheringController(GatheringService gatheringService) {
		this.gatheringService = gatheringService;
	}

	// 1. 관리자용 모임 목록 조회 (페이징 및 신고 우선 필터 지원)
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAdminGatherings(
			@RequestParam(value = "status", defaultValue = "all") String status,
			@RequestParam(value = "keyword", defaultValue = "") String keyword,
			@RequestParam(value = "sortBy", defaultValue = "latest") String sortBy,
			@RequestParam(value = "reportedOnly", defaultValue = "false") boolean reportedOnly,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Map<String, Object> result = gatheringService.getAdminGatherings(status, keyword, sortBy, reportedOnly, page, size);
		return ResponseEntity.ok(result);
	}

	// 2. 모임 상태 변경 (ACTIVE, HIDDEN 등)
	@PutMapping("/{id}/status")
	public ResponseEntity<Map<String, Object>> updateStatus(
			@PathVariable("id") Long roomId,
			@RequestBody Map<String, String> requestBody) {
		try {
			String status = requestBody.get("status");
//			System.out.println("====== Admin status update print ======");
//			System.out.println("roomId: " + roomId + ", status: " + status);
			boolean success = gatheringService.updateGatheringStatus(roomId, status);
//			System.out.println("Update success result: " + success);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "모임 상태가 변경되었습니다."));
			}
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "모임 상태 변경에 실패했습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "오류 발생: " + e.getMessage()));
		}
	}

	// 3. 신고 승인 (선택된 신고 건들 블라인드 처리 및 신고 이력 추가)
	@PostMapping("/{id}/reports/accept")
	public ResponseEntity<Map<String, Object>> acceptReports(
			@PathVariable("id") Long roomId,
			@RequestBody Map<String, Object> requestBody) {
		try {
			List<?> rawIds = (List<?>) requestBody.get("reportIds");
			if (rawIds == null || rawIds.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "처리할 신고 건을 선택해주세요."));
			}
			List<Long> reportIds = rawIds.stream()
					.map(id -> Long.valueOf(id.toString()))
					.toList();
			String adminMemo = requestBody.get("adminMemo") != null ? requestBody.get("adminMemo").toString() : "";
			boolean success = gatheringService.acceptGatheringReports(roomId, reportIds, adminMemo);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "선택한 신고가 승인되어 모임이 블라인드 처리되었습니다."));
			}
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "신고 승인 처리에 실패했습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "오류 발생: " + e.getMessage()));
		}
	}

	// 4. 관리자 메모 저장
	@PutMapping("/{id}/memo")
	public ResponseEntity<Map<String, Object>> saveMemo(
			@PathVariable("id") Long roomId,
			@RequestBody Map<String, String> requestBody) {
		String adminMemo = requestBody.get("adminMemo");
		boolean success = gatheringService.saveGatheringAdminMemo(roomId, adminMemo);
		if (success) {
			return ResponseEntity.ok(Map.of("success", true, "message", "관리자 메모가 수정되었습니다."));
		}
		return ResponseEntity.badRequest().body(Map.of("success", false, "message", "관리자 메모 수정에 실패했습니다."));
	}

	// 5. 모임 관리자 강제 영구 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteGathering(@PathVariable("id") Long roomId) {
		boolean success = gatheringService.deleteGatheringByAdmin(roomId);
		if (success) {
			return ResponseEntity.ok(Map.of("success", true, "message", "모임이 영구 삭제되었습니다."));
		}
		return ResponseEntity.badRequest().body(Map.of("success", false, "message", "모임 삭제에 실패했습니다."));
	}

	// 6. 특정 모임의 개별 신고 목록 조회
	@GetMapping("/{id}/reports")
	public ResponseEntity<List<Map<String, Object>>> getGatheringReports(@PathVariable("id") Long roomId) {
		List<Map<String, Object>> reports = gatheringService.getReportsByRoomId(roomId);
		return ResponseEntity.ok(reports);
	}

	// 7. 특정 모임의 선택된 신고 반려
	@PostMapping("/{id}/reports/reject")
	public ResponseEntity<Map<String, Object>> rejectReports(
			@PathVariable("id") Long roomId,
			@RequestBody Map<String, Object> requestBody) {
		try {
			List<?> rawIds = (List<?>) requestBody.get("reportIds");
			if (rawIds == null || rawIds.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "처리할 신고 건을 선택해주세요."));
			}
			List<Long> reportIds = rawIds.stream()
					.map(id -> Long.valueOf(id.toString()))
					.toList();
			boolean success = gatheringService.rejectGatheringReports(reportIds);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "선택한 신고가 반려 처리되었습니다."));
			}
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "신고 반려 처리에 실패했습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "오류 발생: " + e.getMessage()));
		}
	}

}
