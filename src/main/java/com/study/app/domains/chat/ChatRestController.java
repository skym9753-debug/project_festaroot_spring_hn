package com.study.app.domains.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.chat.dto.ChatMessageDocument;
import com.study.app.domains.chat.dto.ChatPrivateRequestDTO;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController { // 채팅 웹소켓용

	private final ChatService chatService;

	public ChatRestController(ChatService chatService) {
		this.chatService = chatService;
	}

	// 특정 채팅방의 과거 대화 내역 조회(Oracle의 가입 시간 'joined_at' 이후의 MongoDB 메시지만 필터링해서 가져옴)
	@GetMapping("/rooms/{room_id}/messages")
	public ResponseEntity<?> getChatMessages(@PathVariable("room_id") Long roomId,
			@RequestParam("member_id") String memberId) {
		try {
			List<ChatMessageDocument> history = chatService.getChatHistory(roomId, memberId);
			return ResponseEntity.ok(history);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "채팅 내역을 불러오는 중 오류가 발생했습니다."));
		}
	}

	// 채팅 읽음 상태 연동
	@GetMapping("/{userId}")
	public ResponseEntity<?> updateReadStatus(@PathVariable("userId") String userId,
			@RequestParam(value = "activeChatId", required = false) Long activeChatId) {

		try {
			if (activeChatId != null) {
				chatService.updateLastReadAt(activeChatId, userId);
				// System.out.println("읽음 상태 DB 동기화 완료 - 유저 ID: " + userId + ", 활성화된 방 ID: " +
				// activeChatId);
			}
			return ResponseEntity.ok(Map.of("message", "읽음 상태가 동기화되었습니다.", "userId", userId, "activeChatId",
					activeChatId != null ? activeChatId : "none"));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "읽음 상태 갱신 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/private")
	public ResponseEntity<Map<String, Object>> createOrGetPrivateRoom(@RequestBody ChatPrivateRequestDTO dto) {

		// 서비스 단에서 기존 방 조회 또는 신규 생성 후 room_id 반환
		Long roomId = chatService.getOrCreatePrivateRoom(dto.getCurrentUserId(), dto.getTargetMemberId());

		Map<String, Object> response = new HashMap<>();
		response.put("room_id", roomId);

		return ResponseEntity.ok(response);
	}
}