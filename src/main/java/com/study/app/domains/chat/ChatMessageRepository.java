package com.study.app.domains.chat;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

// MongoRepository<사용할Document클래스, Document 클래스 중 PK(id)의 데이터타입>을 상속받기
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String>{
	// 특정 채팅방(roomId)의 모든 메시지를 시간순(createdAt)으로 가져오는 메서드
    List<ChatMessageDocument> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}
