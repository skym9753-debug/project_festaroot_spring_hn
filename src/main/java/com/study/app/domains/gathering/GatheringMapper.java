package com.study.app.domains.gathering;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GatheringMapper {
	
    int insertGathering(GatheringCreateDTO gathering);
    int insertRoomUser(@Param("room_id") Long roomId, @Param("member_id") String memberId);
    
    // 🌟 자유 모임 페이징 및 카운트
    List<Map<String, Object>> selectGatheringList(@Param("offset") int offset, @Param("size") int size);
    int selectGatheringCount();
    
    GatheringCreateDTO selectGatheringDetail(@Param("roomId") Long roomId);
    List<Map<String, Object>> selectParticipants(@Param("room_id") Long roomId);
    Map<String, Object> getRoomCapacityInfo(@Param("room_id") Long roomId);
    int insertParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);
    int deleteParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);

    // 🌟 축제 모임 페이징 및 카운트
    List<Map<String, Object>> selectFestivalGatheringList(@Param("memberId") String memberId, @Param("offset") int offset, @Param("size") int size);
    int selectFestivalGatheringCount();
    
    // 🌟 참여중인 모임 페이징, 필터 및 카운트
    List<Map<String, Object>> selectJoinedGatheringList(@Param("memberId") String memberId, @Param("offset") int offset, @Param("size") int size, @Param("filter") String filter);
    int selectJoinedGatheringCount(@Param("memberId") String memberId, @Param("filter") String filter);
}