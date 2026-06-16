package com.study.app.domains.chat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.chat.dto.ChatMessageDocument;
import com.study.app.domains.gathering.GatheringMapper;

@Service
public class ChatService {

	@Autowired
	private ChatRoomMapper chatRoomMapper;

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

	// 1:1 채팅방 존재 확인 및 생성
	@Transactional
	public Long getOrCreatePrivateRoom(String userA, String userB) {
		// 1. 이미 존재하는 1:1 채팅방이 있는지 검사
		Long existingRoomId = chatRoomMapper.findPrivateRoomBetweenUsers(userA, userB);
		if (existingRoomId != null) {
			return existingRoomId;
		}

		// 2. 존재하지 않는다면 신규 채팅방 마스터 생성 (room_type = 'PRIVATE')
		Long newRoomId = chatRoomMapper.getNextRoomIdSequence();
		chatRoomMapper.insertChatRoom(newRoomId, "DIRECT", "1:1 채팅방");

		// 3. 채팅방 참여 멤버에 두 사람 매핑 등록
		chatRoomMapper.insertChatRoomMember(newRoomId, userA);
		chatRoomMapper.insertChatRoomMember(newRoomId, userB);

		return newRoomId;
	}
}
