package com.study.app.domains.gathering;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatheringService {

	@Autowired
    private GatheringMapper gatheringMapper;

	// 자유 모임 생성
    @Transactional // 방 생성과 방장 참여에서 하나라도 에러 나면 자동 롤백
    public Long createGathering(GatheringCreateDTO dto) {
        
        // CHAT_ROOM 테이블에 데이터 삽입
        // (실행 후 xml의 <selectKey>에 의해 dto.roomId에 시퀀스 번호가 주입됨)
        gatheringMapper.insertGathering(dto);
        
        // 발급된 방 번호와 방장 ID를 매퍼에 던져서 참여자(CHAT_ROOM_USER)로 등록
        Long newRoomId = dto.getRoom_id();
        String ownerId = dto.getOwner_id();
        
        gatheringMapper.insertRoomUser(newRoomId, ownerId);
        
        // 컨트롤러에게 방 번호 전달
        return newRoomId;
    }
    
    // 자유 모임 조회
    public List<GatheringCreateDTO> selectGatheringList(){
    	List<GatheringCreateDTO> result = gatheringMapper.selectGatheringList();
    	return result;
    }
    
    // 자유 모임 상세 조회
    public GatheringCreateDTO selectGatheringDetail(Long roomId) {
    	 return gatheringMapper.selectGatheringDetail(roomId);
    	
    }
	
}
