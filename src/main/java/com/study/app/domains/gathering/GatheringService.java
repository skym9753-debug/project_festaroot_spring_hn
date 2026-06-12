package com.study.app.domains.gathering;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatheringService {

	@Autowired
	private GatheringMapper gatheringMapper;

	// 자유 모임 생성
	@Transactional
	public Long createGathering(GatheringCreateDTO dto) {
		gatheringMapper.insertGathering(dto);
		Long newRoomId = dto.getRoom_id();
		String ownerId = dto.getOwner_id();
		gatheringMapper.insertRoomUser(newRoomId, ownerId);
		return newRoomId;
	}

	// 자유 모임 조회
	public List<GatheringCreateDTO> selectGatheringList() {
		return gatheringMapper.selectGatheringList();
	}

	// 축제 모임 전체 목록 조회
	public List<Map<String, Object>> selectFestivalGatheringList(String memberId) {
		return gatheringMapper.selectFestivalGatheringList(memberId);
	}

	// 자유 및 축제 통합 상세 조회
	public GatheringCreateDTO selectGatheringDetail(Long roomId) {
		return gatheringMapper.selectGatheringDetail(roomId);
	}

	// 참여자 목록 상세 조회
	public List<Map<String, Object>> getParticipants(Long roomId) {
		if (roomId <= 0) {
			return List.of(); // 아직 만들어지지 않은 축제방은 참여자가 당연히 0명
		}
		return gatheringMapper.selectParticipants(roomId);
	}

	// 모임 참여하기
	@Transactional(rollbackFor = Exception.class)
	public Long joinGathering(Long roomId, String memberId) throws Exception {
	    
	    // 1. 만약 음수 ID가 들어왔다면? 아직 DB에 CHAT_ROOM 데이터가 없는 상태 -> 서비스단에서 직접 빌드해서 인서트 진행!
	    if (roomId <= 0) {
	        Long contentId = Math.abs(roomId); // 원래 축제 코드 획득
	        
	        GatheringCreateDTO officialRoom = new GatheringCreateDTO();
	        officialRoom.setFestival_id(contentId);
	        
	        // 🌟 [수정 1] 타입을 GROUP이 아닌 'FESTIVAL'로 정확히 지정합니다. (자유모임 노출 방지)
	        officialRoom.setRoom_type("FESTIVAL"); 
	        
	        officialRoom.setOwner_id(null); // 공식 방이므로 방장(Owner)은 존재하지 않음
	        
	        // 🌟 [수정 2] 하드코딩된 더미 문자열을 지우고 null(또는 세팅 제외)로 둡니다.
	        // 이렇게 비워놔야 매퍼의 NVL(cr.room_title, fi.title || ' 공식 모임')이 축제 테이블(fi)에서 진짜 이름을 가져옵니다.
	        officialRoom.setRoom_title(null);
	        officialRoom.setRoom_description(null);
	        officialRoom.setFree_location(null);
	        officialRoom.setFree_date(null);
	        
	        officialRoom.setMax_capacity(500); // 기본 정원 여유롭게 제공
	        
	        gatheringMapper.insertGathering(officialRoom);
	        roomId = officialRoom.getRoom_id(); // selectKey에 의해 갓 발급된 실제 시퀀스 ID 맵핑
	    }

	    // 2. 현재 인원 및 최대 정원 실시간 확인 (방금 생성되었거나 기존에 있던 방 모두 수행)
	    Map<String, Object> room = gatheringMapper.getRoomCapacityInfo(roomId);
	    if (room == null) return null;

	    int currentCount = Integer.parseInt(room.get("CURRENT_COUNT").toString());
	    int maxCapacity = Integer.parseInt(room.get("MAX_CAPACITY").toString());

	    // 정원 초과 검증
	    if (currentCount >= maxCapacity) {
	        return null;
	    }

	    // 3. 참여자 테이블(CHAT_ROOM_USER)에 세션 유저 추가
	    gatheringMapper.insertParticipant(roomId, memberId);

	    return roomId; // 가입 완료된 실제 방 ID 반환
	}

	// 모임 탈퇴
	@Transactional(rollbackFor = Exception.class)
	public boolean leaveGathering(Long roomId, String memberId) throws Exception {
		if (roomId <= 0) {
			return false; // 생성조차 안 된 방은 탈퇴 불가
		}
		
		int deletedRows = gatheringMapper.deleteParticipant(roomId, memberId);
		if (deletedRows == 0) {
			return false;
		}
		return true;
	}
	
	// 참여중인 모임 목록
	public List<Map<String, Object>> getJoinedGatherings(String memberId) {
	    return gatheringMapper.selectJoinedGatheringList(memberId);
	}
}