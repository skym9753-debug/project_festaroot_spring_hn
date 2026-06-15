package com.study.app.domains.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;


// MongoRepository<사용할Document클래스, Document 클래스 중 PK(id)의 데이터타입>을 상속받기
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
	// 특정 채팅방(roomId)의 모든 메시지를 시간순(createdAt)으로 가져오는 메서드
	List<ChatMessageDocument> findByRoomIdOrderByCreatedAtAsc(Long roomId);

	// room_id가 일치하고, 작성 시간(createdAt)이 유저 입장 시간(joinedAt) 이후인 메시지만 조회
	List<ChatMessageDocument> findByRoomIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(Long roomId,
			LocalDateTime joinedAt);

	// 특정 방의 가장 최근 메시지 딱 1개만 가져오기 (마지막 메시지용)
	Optional<ChatMessageDocument> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);

	// 특정 방에서 유저가 마지막으로 읽은 시간(lastReadAt)보다 나중에 온 메시지 개수 카운트 (안읽은 메시지 수)
	long countByRoomIdAndCreatedAtGreaterThan(Long roomId, LocalDateTime lastReadAt);
}
