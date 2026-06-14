package com.study.app.domains.chat;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatRestController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // 특정 채팅방의 과거 대화 내역을 시간순(Asc)으로 전부 조회합니다.
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDocument>> getChatHistory(@PathVariable("roomId") Long roomId) {
        List<ChatMessageDocument> history = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        return ResponseEntity.ok(history);
    }
}