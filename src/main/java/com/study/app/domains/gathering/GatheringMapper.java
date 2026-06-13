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
    
    // 자유 모임 목록 조회 (페이징 지원)
    List<Map<String, Object>> selectGatheringList(@Param("member_id") String memberId, @Param("page") int page, @Param("size") int size);
    
    // 자유 모임 총 개수 조회
    int countGatheringList();
    
    // 자유 모임 및 축제 모임 통합 상세 조회 (음수/양수 대응 고도화)
    GatheringCreateDTO selectGatheringDetail(@Param("roomId") Long roomId);
    
    // 모임 참여자 목록 상세 조회
    List<Map<String, Object>> selectParticipants(@Param("room_id") Long roomId);
    
    // 모임 수정
    int updateGathering(GatheringCreateDTO gathering);
    
    // 모임 삭제
    int deleteGathering(@Param("room_id") Long roomId);
    
    // 모임 참여자 전체 삭제 (방 삭제 전처리)
    int deleteAllParticipants(@Param("room_id") Long roomId);
    
    // 방장 위임
    int updateOwner(@Param("room_id") Long roomId, @Param("new_owner_id") String newOwnerId);
    
    // 모임 정원 및 현재 인원 조회
 	Map<String, Object> getRoomCapacityInfo(@Param("room_id") Long roomId);
 
 	// 모임 참여자 등록
 	int insertParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);
 	
 	// 모임 탈퇴 (참여자 제거)
 	int deleteParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);

 	// 축제 모임 전체 목록 조회 (축제 전체 기준 + 채팅방 조인) (페이징 지원)
 	List<Map<String, Object>> selectFestivalGatheringList(@Param("member_id") String memberId, @Param("page") int page, @Param("size") int size);
 	
 	// 축제 모임 총 개수 조회
 	int countFestivalGatheringList();
 	
 	// 참여중인 모임 목록 (페이징 지원)
 	List<Map<String,Object>> selectJoinedGatheringList(@Param("memberId") String memberId, @Param("page") int page, @Param("size") int size, @Param("filter") String filter);

    // 참여중인 모임 총 개수 조회
    int countJoinedGatheringList(@Param("memberId") String memberId, @Param("filter") String filter);
}