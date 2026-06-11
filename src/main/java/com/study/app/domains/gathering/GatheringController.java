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
			// 생성된 방 번호 반환
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

	// 자유 모임 조회
	@GetMapping("/list")
	public ResponseEntity<?> selectGatheringList() {
		List<GatheringCreateDTO> result = gatheringService.selectGatheringList();
		return ResponseEntity.ok(result);
	}

	// 자유 모임 상세 조회
	// GET /api/gathering/12 구조로 호출됨
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

}
