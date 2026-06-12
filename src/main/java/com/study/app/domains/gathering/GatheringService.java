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

	// 자유 모임 조회 (페이징 지원)
	public Map<String, Object> selectGatheringList(int page, int size) {
		int totalCount = gatheringMapper.countGatheringList();
		List<Map<String, Object>> list = gatheringMapper.selectGatheringList(page, size);
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("pageInfo", getPageInfo(page, size, totalCount));
		
		return result;
	}

	// 축제 모임 전체 목록 조회 (페이징 지원)
	public Map<String, Object> selectFestivalGatheringList(String memberId, int page, int size) {
		int totalCount = gatheringMapper.countFestivalGatheringList();
		List<Map<String, Object>> list = gatheringMapper.selectFestivalGatheringList(memberId, page, size);
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("pageInfo", getPageInfo(page, size, totalCount));
		
		return result;
	}
	
	// 참여중인 모임 목록 (페이징 및 필터 지원)
	public Map<String, Object> getJoinedGatherings(String memberId, int page, int size, String filter) {
		int totalCount = gatheringMapper.countJoinedGatheringList(memberId, filter);
		List<Map<String, Object>> list = gatheringMapper.selectJoinedGatheringList(memberId, page, size, filter);
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("pageInfo", getPageInfo(page, size, totalCount));
		
		return result;
	}
	
	// 페이징 정보 생성 공통 로직
	private Map<String, Object> getPageInfo(int currentPage, int size, int totalCount) {
		int pageBlock = 5;
		int totalPage = (int) Math.ceil((double) totalCount / size);
		int endPage = (int) (Math.ceil(currentPage / (double) pageBlock)) * pageBlock;
		int startPage = endPage - pageBlock + 1;
		
		if (endPage > totalPage) endPage = totalPage;
		if (startPage < 1) startPage = 1;
		
		boolean existPrev = startPage > 1;
		boolean existNext = endPage < totalPage;
		
		Map<String, Object> pageInfo = new HashMap<>();
		pageInfo.put("currentPage", currentPage);
		pageInfo.put("startPage", startPage);
		pageInfo.put("endPage", endPage);
		pageInfo.put("totalPage", totalPage);
		pageInfo.put("totalCount", totalCount);
		pageInfo.put("existPrev", existPrev);
		pageInfo.put("existNext", existNext);
		
		return pageInfo;
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
}