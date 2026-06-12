package com.study.app.domains.gathering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatheringService {

	@Autowired
	private GatheringMapper gatheringMapper;

	@Transactional
	public Long createGathering(GatheringCreateDTO dto) {
		gatheringMapper.insertGathering(dto);
		Long newRoomId = dto.getRoom_id();
		String ownerId = dto.getOwner_id();
		gatheringMapper.insertRoomUser(newRoomId, ownerId);
		return newRoomId;
	}

	// 🌟 자유 모임 조회 페이징 고도화
	public Map<String, Object> selectGatheringList(int page, int size) {
		int offset = (page - 1) * size;
		List<Map<String, Object>> list = gatheringMapper.selectGatheringList(offset, size);
		int totalCount = gatheringMapper.selectGatheringCount();
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("total_count", totalCount);
		return result;
	}

	// 🌟 축제 모임 전체 목록 페이징 고도화
	public Map<String, Object> selectFestivalGatheringList(String memberId, int page, int size) {
		int offset = (page - 1) * size;
		List<Map<String, Object>> list = gatheringMapper.selectFestivalGatheringList(memberId, offset, size);
		int totalCount = gatheringMapper.selectFestivalGatheringCount();
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("total_count", totalCount);
		return result;
	}

	public GatheringCreateDTO selectGatheringDetail(Long roomId) {
		return gatheringMapper.selectGatheringDetail(roomId);
	}

	public List<Map<String, Object>> getParticipants(Long roomId) {
		if (roomId <= 0) return List.of();
		return gatheringMapper.selectParticipants(roomId);
	}

	@Transactional(rollbackFor = Exception.class)
	public Long joinGathering(Long roomId, String memberId) throws Exception {
	    if (roomId <= 0) {
	        Long contentId = Math.abs(roomId);
	        GatheringCreateDTO officialRoom = new GatheringCreateDTO();
	        officialRoom.setFestival_id(contentId);
	        officialRoom.setRoom_type("FESTIVAL"); 
	        officialRoom.setOwner_id(null);
	        officialRoom.setRoom_title(null);
	        officialRoom.setRoom_description(null);
	        officialRoom.setFree_location(null);
	        officialRoom.setFree_date(null);
	        officialRoom.setMax_capacity(500);
	        
	        gatheringMapper.insertGathering(officialRoom);
	        roomId = officialRoom.getRoom_id();
	    }

	    Map<String, Object> room = gatheringMapper.getRoomCapacityInfo(roomId);
	    if (room == null) return null;

	    int currentCount = Integer.parseInt(room.get("CURRENT_COUNT").toString());
	    int maxCapacity = Integer.parseInt(room.get("MAX_CAPACITY").toString());

	    if (currentCount >= maxCapacity) return null;

	    gatheringMapper.insertParticipant(roomId, memberId);
	    return roomId;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean leaveGathering(Long roomId, String memberId) throws Exception {
		if (roomId <= 0) return false;
		int deletedRows = gatheringMapper.deleteParticipant(roomId, memberId);
		return deletedRows != 0;
	}
	
	// 🌟 참여중인 모임 페이징 및 필터 고도화
	public Map<String, Object> getJoinedGatherings(String memberId, int page, int size, String filter) {
		int offset = (page - 1) * size;
	    List<Map<String, Object>> list = gatheringMapper.selectJoinedGatheringList(memberId, offset, size, filter);
	    int totalCount = gatheringMapper.selectJoinedGatheringCount(memberId, filter);
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("list", list);
	    result.put("total_count", totalCount);
	    return result;
	}
}