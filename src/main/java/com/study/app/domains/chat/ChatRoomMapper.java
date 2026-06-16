package com.study.app.domains.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatRoomMapper {

	// 두 유저 간의 기존 DIRECT 채팅방이 존재하는지 확인
	Long findPrivateRoomBetweenUsers(@Param("userA") String userA, @Param("userB") String userB);

	// 새 채팅방 마스터 ID 채번
	Long getNextRoomIdSequence();

	// 채팅방 마스터 정보 삽입
	int insertChatRoom(@Param("roomId") Long roomId, @Param("roomType") String roomType,
			@Param("roomTitle") String roomTitle);

	// 채팅방 참여 유저 등록
	int insertChatRoomMember(@Param("roomId") Long roomId, @Param("memberId") String memberId);

	// 두 사용자 간에 차단 내역이 존재하는지 확인 (count 가 0보다 크면 차단 상태)
	int checkBlockStatus(@Param("userA") String userA, @Param("userB") String userB);

	// 차단 내역 등록
	int insertBlockUser(@Param("memberId") String memberId, @Param("blockedId") String blockedId);

	// 채팅방 유저 매핑 삭제 (방 나가기)
	int deleteChatRoomUser(@Param("roomId") Long roomId, @Param("memberId") String memberId);

	// 해당 채팅방에 현재 남아있는 유저 수 조회
	int countUsersInRoom(@Param("roomId") Long roomId);

	// 채팅방 마스터 삭제 (아무도 없을 때 청소)
	int deleteChatRoom(@Param("roomId") Long roomId);

	// 특정 유저가 참여하고 있는 모든 채팅방 목록 조회
	List<Map<String, Object>> getChatRoomsByUserId(@Param("userId") String userId);

	// 메세지 읽음 확인
	LocalDateTime getLastReadAt(@Param("roomId") Long roomId, @Param("memberId") String memberId);
}