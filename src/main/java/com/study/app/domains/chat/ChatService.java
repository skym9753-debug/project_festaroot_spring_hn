package com.study.app.domains.chat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.chat.dto.ChatMessageDocument;
import com.study.app.domains.gathering.GatheringMapper;

@Service
public class ChatService {

	@Autowired
	private GatheringMapper gatheringMapper; // Oracle Mapper

	@Autowired
	private ChatMessageRepository chatMessageRepository; // MongoDB Repository

	public List<ChatMessageDocument> getChatHistory(Long roomId, String memberId) {
		// 1. Oracle DB에서 해당 유저의 채팅방 입장 시간을 가져옵니다.
		LocalDateTime joinedAt = gatheringMapper.selectUserJoinedAt(roomId, memberId);

		// 방어 코드: 만약 입장 기록이 없다면 빈 리스트 반환
		if (joinedAt == null) {
			return Collections.emptyList();
		}

		// 2. 그 시간 이후의 MongoDB 메시지만 필터링하여 가져옵니다.
		return chatMessageRepository.findByRoomIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(roomId, joinedAt);
	}

	// 읽은 메세지
	public void updateLastReadAt(Long roomId, String memberId) {
		gatheringMapper.updateLastReadAt(roomId, memberId);
	}
}
