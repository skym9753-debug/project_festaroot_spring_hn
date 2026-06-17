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

	@PostMapping("/direct")
	public ResponseEntity<?> createOrGetPrivateRoom(@RequestBody ChatPrivateRequestDTO dto) {
		try {
			// 서비스단에서 차단 예외 발생 시 catch 하도록 구조 변경
			Long roomId = chatService.getOrCreatePrivateRoom(dto.getCurrentUserId(), dto.getTargetMemberId());
			Map<String, Object> response = new HashMap<>();
			response.put("room_id", roomId);
			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			// 임포트 없이 숫자로 직관적으로 403을 넘기는 방법
			return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류가 발생했습니다."));
		}
	}

	// 1:1 채팅방 퇴장 및 차단 요청 처리 API
	@PostMapping("/rooms/{room_id}/leave")
	public ResponseEntity<?> leavePrivateRoom(@PathVariable("room_id") Long roomId,
			@RequestBody Map<String, Object> payload) {
		try {
			String memberId = (String) payload.get("memberId");
			boolean isBlock = (boolean) payload.get("isBlock");
			String targetMemberId = (String) payload.get("targetMemberId");

			chatService.leavePrivateRoom(roomId, memberId, isBlock, targetMemberId);
			return ResponseEntity.ok(Map.of("message", "채팅방 퇴장 처리가 완료되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "퇴장 처리 중 오류가 발생했습니다."));
		}
	}
	
	// 채팅방 목록
	@GetMapping("/rooms/user/{userId}")
	public ResponseEntity<?> getUserChatRooms(@PathVariable("userId") String userId) {

	    try {
	        List<Map<String, Object>> roomList = chatService.getUserChatRoomList(userId);

	        return ResponseEntity.ok(roomList);

	    } catch (Exception e) {

	        e.printStackTrace();

	        return ResponseEntity.internalServerError().body(
	            Map.of(
	                "message", "채팅방 목록을 불러오는 중 오류가 발생했습니다.",
	                "error", e.getMessage()
	            )
	        );
	    }
	}
}