package com.study.app.domains.gathering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gathering")
public class GatheringController {

	@Autowired
	private GatheringService gatheringService;

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

	// 자유 모임 리스트 조회
	@GetMapping("/list")
	public ResponseEntity<?> selectGatheringList() {
		List<GatheringCreateDTO> result = gatheringService.selectGatheringList();
		return ResponseEntity.ok(result);
	}

	// 축제 모임 전체 목록 조회 (로그인 유저가 있다면 찜/가입 판별용 파라미터 수신)
	@GetMapping("/festival")
	public ResponseEntity<?> selectFestivalGatheringList(@RequestParam(value = "memberId", required = false) String memberId) {
		List<Map<String, Object>> result = gatheringService.selectFestivalGatheringList(memberId);
		return ResponseEntity.ok(result);
	}

	// 자유 모임 & 축제 모임 통합 상세 조회
	@GetMapping("/{room_id}")
	public ResponseEntity<GatheringCreateDTO> getGatheringDetail(@PathVariable("room_id") Long roomId) {
		GatheringCreateDTO detail = gatheringService.selectGatheringDetail(roomId);
		if (detail == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(detail);
	}

	// 특정 방의 순수 참여자 목록 가져오기 (방장 제외)
	@GetMapping("/{room_id}/participants")
	public ResponseEntity<List<Map<String, Object>>> getParticipants(@PathVariable("room_id") Long roomId) {
		List<Map<String, Object>> participants = gatheringService.getParticipants(roomId);
		return ResponseEntity.ok(participants);
	}
	
	// 모임 참여하기 (동적 생성 연동 스펙 고도화)
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinGathering(@PathVariable("roomId") Long roomId, @RequestBody Map<String, Object> payload) {
    	String memberId = payload.get("member_id").toString();
        try {
            Long actualRoomId = gatheringService.joinGathering(roomId, memberId);
            if (actualRoomId != null) {
                return ResponseEntity.ok(Map.of(
                	"success", true,
                	"message", "모임 참여가 완료되었습니다.",
                	"roomId", actualRoomId // 💡 프론트엔드가 URL 패스를 갈아끼울 수 있도록 리턴
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "정원이 가득 찼습니다."));
            }
        } catch (Exception e) {
        	return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류입니다."));
        }
    }

    // 모임 나가기
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
    
    // 참여중인 모임 목록
    @GetMapping("/joined")
    public ResponseEntity<?> getJoinedGatherings(@RequestParam("member_id") String memberId) {
        List<Map<String, Object>> result = gatheringService.getJoinedGatherings(memberId);
        return ResponseEntity.ok(result);
    }
}