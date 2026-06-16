package com.study.app.domains.chat;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatRoomMapper {

    // 1. 두 유저 간의 기존 PRIVATE 채팅방이 존재하는지 확인
    Long findPrivateRoomBetweenUsers(@Param("userA") String userA, @Param("userB") String userB);

    // 2. 새 채팅방 마스터 ID 채번
    Long getNextRoomIdSequence();

    // 3. 채팅방 마스터 정보 삽입
    int insertChatRoom(@Param("roomId") Long roomId, @Param("roomType") String roomType, @Param("roomTitle") String roomTitle);

    // 4. 채팅방 참여 유저 등록
    int insertChatRoomMember(@Param("roomId") Long roomId, @Param("memberId") String memberId);
}