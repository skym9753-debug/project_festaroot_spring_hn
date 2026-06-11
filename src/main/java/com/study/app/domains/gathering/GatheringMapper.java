package com.study.app.domains.gathering;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GatheringMapper {
	
	// 새 모임/채팅방 개설
    int insertGathering(GatheringCreateDTO gathering);

    // 채팅방 참여자 등록 (방장 자동 참여용) -> @Param은 DAO에서 Map 대신 사용
    int insertRoomUser(@Param("room_id") Long roomId, @Param("member_id") String memberId);
    
    // 자유 모임 목록 조회
    List<GatheringCreateDTO> selectGatheringList();
    
    // 자유 모임 상세 조회
    GatheringCreateDTO selectGatheringDetail(Long room_id);
    
    // 자유 모임 참여자 목록 상세 조회
    List<Map<String, Object>> selectParticipants(Long roomId);
}
