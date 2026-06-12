package com.study.app.domains.gathering;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GatheringMapper {
	
	// 새 모임/채팅방 개설
    int insertGathering(GatheringCreateDTO gathering);

    // 채팅방 참여자 등록 (방장 자동 참여용)
    int insertRoomUser(@Param("room_id") Long roomId, @Param("member_id") String memberId);
    
    // 자유 모임 목록 조회
    List<GatheringCreateDTO> selectGatheringList();
    
    // 자유 모임 및 축제 모임 통합 상세 조회 (음수/양수 대응 고도화)
    GatheringCreateDTO selectGatheringDetail(@Param("roomId") Long roomId);
    
    // 모임 참여자 목록 상세 조회
    List<Map<String, Object>> selectParticipants(@Param("room_id") Long roomId);
    
    // 모임 정원 및 현재 인원 조회
 	Map<String, Object> getRoomCapacityInfo(@Param("room_id") Long roomId);
 
 	// 모임 참여자 등록
 	int insertParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);
 	
 	// 모임 탈퇴 (참여자 제거)
 	int deleteParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);

 	// 축제 모임 전체 목록 조회 (축제 전체 기준 + 채팅방 조인)
 	List<Map<String, Object>> selectFestivalGatheringList(@Param("memberId") String memberId);
 	
 	// 참여중인 모임 목록
 	List<Map<String,Object>> selectJoinedGatheringList(String memberId);
}