package com.study.app.domains.gathering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.study.app.domains.storage.uploadService;

@RestController
@RequestMapping("/api/gathering")
public class GatheringController {

	@Autowired
	private GatheringService gatheringService;

	@Autowired
	private uploadService uploadService;

	// 모임 이미지 업로드
	@PostMapping("/image")
	public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			String imageUrl = uploadService.upload(file, "gathering");
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("imageUrl", imageUrl);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("이미지 업로드 실패: " + e.getMessage());
		}
	}

	// 자유 모임 생성
	@PostMapping
	public ResponseEntity<?> createGathering(@RequestBody GatheringCreateDTO dto) {
		try {
			Long roomId = gatheringService.createGathering(dto);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("roomId", roomId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("모임 생성 실패: " + e.getMessage());
		}
	}

	// 자유 모임 페이징 목록 조회
	@GetMapping("/list")
	public ResponseEntity<?> selectGatheringList(
			@RequestParam("member_id") String memberId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {
		Map<String, Object> result = gatheringService.selectGatheringList(memberId,page, size);
		return ResponseEntity.ok(result);
	}

	// 축제 모임 페이징 목록 조회
	@GetMapping("/festival")
	public ResponseEntity<?> selectFestivalGatheringList(
			@RequestParam(value = "member_id", required = false) String memberId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {
		Map<String, Object> result = gatheringService.selectFestivalGatheringList(memberId, page, size);
		return ResponseEntity.ok(result);
	}

	// 모임 상세 보기
	@GetMapping("/{room_id}")
	public ResponseEntity<GatheringCreateDTO> getGatheringDetail(@PathVariable("room_id") Long roomId) {
		GatheringCreateDTO detail = gatheringService.selectGatheringDetail(roomId);
		
		if (detail == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(detail);
	}

	// 모임 참여자 목록 조회
	@GetMapping("/{room_id}/participants")
	public ResponseEntity<List<Map<String, Object>>> getParticipants(@PathVariable("room_id") Long roomId) {
		List<Map<String, Object>> participants = gatheringService.getParticipants(roomId);
		return ResponseEntity.ok(participants);
	}
	
	// 모임 참여하기
	@PostMapping("/{roomId}/join")
	public ResponseEntity<?> joinGathering(@PathVariable("roomId") Long roomId, @RequestBody Map<String, Object> payload) {
		String memberId = payload.get("member_id").toString();
		try {
			Long actualRoomId = gatheringService.joinGathering(roomId, memberId);
			if (actualRoomId != null) {
				return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "모임 참여가 완료되었습니다.",
					"roomId", actualRoomId
				));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "정원이 가득 찼습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}

	// 모임 스스로 나가기
	@PostMapping("/{room_id}/leave")
	public ResponseEntity<?> leaveGathering(@PathVariable("room_id") Long roomId, @RequestBody Map<String, Object> payload) {
		String memberId = payload.get("member_id").toString();
		try {
			boolean success = gatheringService.leaveGathering(roomId, memberId);
			if (success) {
				return ResponseEntity.ok(Map.of("message", "모임에서 정상적으로 탈퇴되었습니다."));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "참여 정보가 존재하지 않거나 탈퇴할 수 없습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}
    
	// [RESTful] 모임 정보 수정
	@PutMapping("/{room_id}") 
	public ResponseEntity<?> updateGathering(@PathVariable("room_id") Long roomId, @RequestBody GatheringCreateDTO dto) {
		try {
			dto.setRoom_id(roomId);
			boolean success = gatheringService.updateGathering(dto);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "모임 정보가 수정되었습니다."));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "모임 수정에 실패했습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}

	// [RESTful] 모임 삭제 (DELETE 규칙에 따라 Body 대신 Query Parameter 사용)
	@DeleteMapping("/{room_id}")
	public ResponseEntity<?> deleteGathering(@PathVariable("room_id") Long roomId, @RequestParam("owner_id") String ownerId) {
		try {
			boolean success = gatheringService.deleteGathering(roomId, ownerId);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "모임이 삭제되었습니다."));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "모임을 삭제할 권한이 없거나 삭제에 실패했습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}

	// [RESTful] 방장 권한 위임 (하위 호스트 자원 수정 개념)
	@PutMapping("/{room_id}/host")
	public ResponseEntity<?> transferHost(@PathVariable("room_id") Long roomId, @RequestBody Map<String, Object> payload) {
		String currentOwnerId = payload.get("current_owner_id").toString();
		String newOwnerId = payload.get("new_owner_id").toString();
		try {
			boolean success = gatheringService.transferHost(roomId, currentOwnerId, newOwnerId);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "방장 권한이 위임되었습니다."));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "방장 위임에 실패했습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}

	// [RESTful] 참여자 강퇴 (참여자 리스트 자원 하위에서 특정 멤버를 DELETE)
	@DeleteMapping("/{room_id}/participants/{member_id}")
	public ResponseEntity<?> kickParticipant(
			@PathVariable("room_id") Long roomId, 
			@PathVariable("member_id") String targetMemberId,
			@RequestParam("owner_id") String ownerId) {
		try {
			boolean success = gatheringService.kickParticipant(roomId, ownerId, targetMemberId);
			if (success) {
				return ResponseEntity.ok(Map.of("success", true, "message", "참여자가 강퇴되었습니다."));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "강퇴 처리에 실패했습니다."));
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
		}
	}

	// 참여중인 모임 목록 조회
	@GetMapping("/joined")
	public ResponseEntity<?> getJoinedGatherings(
			@RequestParam("member_id") String memberId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "filter", defaultValue = "전체") String filter) {
		Map<String, Object> result = gatheringService.getJoinedGatherings(memberId, page, size, filter);
		return ResponseEntity.ok(result);
	}
}