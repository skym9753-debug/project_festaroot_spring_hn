package com.study.app.domains.chat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		// 방을 조회하거나 생성하기 전에 두 사람 간의 차단 여부를 먼저 확인
		if (chatRoomMapper.checkBlockStatus(userA, userB) > 0) {
			throw new IllegalStateException("차단된 사용자 간에는 1:1 채팅을 시작할 수 없습니다.");
		}

		// 1. 이미 존재하는 1:1 채팅방이 있는지 검사
		Long existingRoomId = chatRoomMapper.findPrivateRoomBetweenUsers(userA, userB);
		if (existingRoomId != null) {
			return existingRoomId;
		}

		// 2. 존재하지 않는다면 신규 채팅방 마스터 생성
		Long newRoomId = chatRoomMapper.getNextRoomIdSequence();
		chatRoomMapper.insertChatRoom(newRoomId, "DIRECT", "1:1 채팅방");

		// 3. 채팅방 참여 멤버에 두 사람 매핑 등록
		chatRoomMapper.insertChatRoomMember(newRoomId, userA);
		chatRoomMapper.insertChatRoomMember(newRoomId, userB);

		ChatMessageDocument systemMsg = new ChatMessageDocument();

		systemMsg.setRoomId(newRoomId);

		systemMsg.setSenderId("SYSTEM");
		systemMsg.setSenderName("SYSTEM");

		systemMsg.setType(ChatType.DM);

		systemMsg.setMessage("채팅이 시작되었습니다.");

		systemMsg.setCreatedAt(LocalDateTime.now());

		chatMessageRepository.save(systemMsg);

		return newRoomId;
	}

	// 1:1 채팅방 나가기 및 차단 처리 비즈니스 로직
	@Transactional
	public void leavePrivateRoom(Long roomId, String memberId, boolean isBlockRequested, String targetMemberId) {
		// 1. 차단하고 나가기 요청인 경우 차단 데이터 추가
		if (isBlockRequested && targetMemberId != null) {
			chatRoomMapper.insertBlockUser(memberId, targetMemberId);
		}

		// 2. CHAT_ROOM_USER 테이블에서 내 매핑 행 삭제
		chatRoomMapper.deleteChatRoomUser(roomId, memberId);

		// 3. 방에 남은 유저 수가 0명인지 확인 후 청소
		if (chatRoomMapper.countUsersInRoom(roomId) == 0) {
			chatRoomMapper.deleteChatRoom(roomId);
			// 필요시 MongoDB의 해당 방 메시지 내역을 지우는 로직을 여기에 추가해도 됨.
		}
	}

	// 채팅 목록 조회
	public List<Map<String, Object>> getUserChatRoomList(String userId) {
		System.out.println("채팅목록 조회 userId = " + userId);
		// 1. Oracle DB에서 해당 유저가 참여 중인 채팅방 목록을 가져옵니다.
		// (※ 기존에 사용하던 Mapper의 목록 조회 메서드명으로 매칭해줘)
		List<Map<String, Object>> rooms = chatRoomMapper.getChatRoomsByUserId(userId);

		rooms.forEach(room -> {
			System.out.println("rooms 값 확인" + room);
		});

		// 2. 각 채팅방을 순회하며 MongoDB에서 최신 메시지를 꺼내와 조립하고 필터링합니다.
		return rooms.stream().map(room -> {

			System.out.println("room_id = " + room.get("room_id"));

			// DB 타입에 따라 Long 변환 처리
			Long roomId = ((Number) room.get("room_id")).longValue();

			System.out.println("roomId = " + roomId);

			// MongoDB에서 이 방의 가장 최근 메시지 딱 1개 조회
			Optional<ChatMessageDocument> lastMsgOpt = chatMessageRepository
					.findFirstByRoomIdOrderByCreatedAtDesc(roomId);

			if (lastMsgOpt.isPresent()) {
				ChatMessageDocument lastMsg = lastMsgOpt.get();
				room.put("last_message", lastMsg.getMessage());
				room.put("last_message_time", lastMsg.getCreatedAt());
			}
			LocalDateTime lastReadAt = chatRoomMapper.getLastReadAt(roomId, userId);

			long unreadCount = 0;

			if (lastReadAt != null) {

				unreadCount = chatMessageRepository.countByRoomIdAndCreatedAtGreaterThanAndSenderIdNot(roomId,
						lastReadAt, userId);
			}

			room.put("unread_count", unreadCount);

			return room;
		})
//	        .filter(room -> {
//	            String roomType = (String) room.get("room_type");
//	            // 1:1 채팅방(DIRECT)인데 최신 메시지가 없다면 목록에서 제외시킴
//	            if ("DIRECT".equalsIgnoreCase(roomType)) {
//	                return room.get("last_message") != null;
//	            }
//	            // 모임 채팅방은 메시지가 없어도 목록에 보여줌
//	            return true; 
//	        })
				.toList();
	}
}
