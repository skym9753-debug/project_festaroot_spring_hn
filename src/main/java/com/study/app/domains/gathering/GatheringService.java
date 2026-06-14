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

	// 자유 모임 조회 (페이징 및 검색 지원)
	public Map<String, Object> selectGatheringList(String memberId, int page, int size, String keyword) {
		int totalCount = gatheringMapper.countGatheringList(keyword);
		List<Map<String, Object>> list = gatheringMapper.selectGatheringList(memberId, page, size, keyword);
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("pageInfo", getPageInfo(page, size, totalCount));
		
		return result;
	}

	// 축제 모임 전체 목록 조회 (페이징 및 검색 지원)
	public Map<String, Object> selectFestivalGatheringList(String member_id, int page, int size, String keyword) {
		int totalCount = gatheringMapper.countFestivalGatheringList(keyword);
		List<Map<String, Object>> list = gatheringMapper.selectFestivalGatheringList(member_id, page, size, keyword);
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("pageInfo", getPageInfo(page, size, totalCount));
		
		return result;
	}
	
	// 참여중인 모임 목록 (페이징, 필터, 검색 지원)
	public Map<String, Object> getJoinedGatherings(String member_id, int page, int size, String filter, String keyword) {
		int totalCount = gatheringMapper.countJoinedGatheringList(member_id, filter, keyword);
		List<Map<String, Object>> list = gatheringMapper.selectJoinedGatheringList(member_id, page, size, filter, keyword);
		
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

	// 모임 수정
	@Transactional
	public boolean updateGathering(GatheringCreateDTO dto) {
		return gatheringMapper.updateGathering(dto) > 0;
	}

	// 모임 삭제 (방장 권한 확인 필수)
	@Transactional
	public boolean deleteGathering(Long roomId, String ownerId) {
		// 방장인지 확인
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(ownerId)) {
			return false;
		}
		
		// 1. 참여자들 모두 내보내기
		gatheringMapper.deleteAllParticipants(roomId);
		
		// 2. 방 삭제
		return gatheringMapper.deleteGathering(roomId) > 0;
	}

	// 방장 위임
	@Transactional
	public boolean transferHost(Long roomId, String currentOwnerId, String newOwnerId) {
		// 현재 방장이 맞는지 확인
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(currentOwnerId)) {
			return false;
		}
		
		return gatheringMapper.updateOwner(roomId, newOwnerId) > 0;
	}

	// 참여자 강퇴 (밴 등록 로직 연동)
	@Transactional
	public boolean kickParticipant(Long roomId, String ownerId, String targetMemberId) {
		// 방장 권한 확인
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(ownerId)) {
			return false;
		}
		
		// 방장 본인은 강퇴 불가
		if (ownerId.equals(targetMemberId)) {
			return false;
		}
		
		// 1. 재입장 방지를 위해 밴 테이블에 유저 등록
		gatheringMapper.insertBanUser(roomId, targetMemberId);
		
		// 2. 기존 참여자 테이블에서 제거
		return gatheringMapper.deleteParticipant(roomId, targetMemberId) > 0;
	}

	// 유저 밴 여부 확인 (컨트롤러에서 호출)
	public boolean isBanned(Long roomId, String memberId) {
		if (roomId <= 0) return false; // 공식 축제 모임방 등은 예외 처리 가능
		return gatheringMapper.checkBanStatus(roomId, memberId) > 0;
	}

	public GatheringCreateDTO selectGatheringDetail(Long roomId) {
		return gatheringMapper.selectGatheringDetail(roomId);
	}

	public List<Map<String, Object>> getParticipants(Long roomId) {
		if (roomId <= 0) return List.of();
		return gatheringMapper.selectParticipants(roomId);
	}
	
	public List<Map<String, Object>> selectGatheringMembers(Long roomId){
		if(roomId <= 0) return List.of();
		return gatheringMapper.selectGatheringMembers(roomId);
	}

	@Transactional(rollbackFor = Exception.class)
	public Long joinGathering(Long roomId, String member_id) throws Exception {
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

	    gatheringMapper.insertParticipant(roomId, member_id);
	    return roomId;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean leaveGathering(Long roomId, String member_id) throws Exception {
		if (roomId <= 0) return false;
		int deletedRows = gatheringMapper.deleteParticipant(roomId, member_id);
		return deletedRows != 0;
	}
}