package com.study.app.domains.chat;

import java.time.LocalDateTime;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(SimpMessageSendingOperations messagingTemplate, ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat/message")
    public void message(ChatMessageDocument message) {
        
        // 💡 [시간 동기화 핵심] 프론트엔드 시간 대신, 서버에 도달한 현재 시간으로 정확하게 세팅합니다.
        message.setCreatedAt(LocalDateTime.now());

        // 1. 처음 입장하거나 퇴장할 때의 시스템 메시지 텍스트 가공 처리
        if (ChatType.ENTER.equals(message.getType())) {
            message.setMessage(message.getSenderName() + "님이 입장하셨습니다.");
        } else if (ChatType.LEAVE.equals(message.getType())) {
            message.setMessage(message.getSenderName() + "님이 퇴장하셨습니다.");
        }

        // 2. 대량의 실시간 메시지를 몽고DB에 안전하게 영구 저장합니다.
        chatMessageRepository.save(message);

        // 3. 해당 채팅방 주소를 구독하는 모든 클라이언트에게 실시간 브로드캐스팅!
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}