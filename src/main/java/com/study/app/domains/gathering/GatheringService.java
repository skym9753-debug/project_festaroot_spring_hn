package com.study.app.domains.gathering;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.chat.ChatMessageDocument;
import com.study.app.domains.chat.ChatMessageRepository;
import com.study.app.domains.member.MemberDAO;

@Service
public class GatheringService {

	@Autowired
	private GatheringMapper gatheringMapper;

	@Autowired
	private MemberDAO memberDao;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

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

	// 참여중인 모임 목록 (페이징, 필터, 검색 지원 + 실시간 채팅 메타데이터 추가)
	public Map<String, Object> getJoinedGatherings(String member_id, int page, int size, String filter,
			String keyword) {
		int totalCount = gatheringMapper.countJoinedGatheringList(member_id, filter, keyword);
		// 1. Oracle에서 기본 참여 방 목록 조회
		List<Map<String, Object>> list = gatheringMapper.selectJoinedGatheringList(member_id, page, size, filter,
				keyword);

		// 각 방마다 MongoDB를 조회하여 마지막 메시지와 안읽은 카운트를 주입합니다.
		for (Map<String, Object> room : list) {
			// Oracle 쿼리 결과에서 room_id 추출 (안전하게 문자열 변환 후 Long 타입 변환)
			Long roomId = Long.valueOf(String.valueOf(room.get("room_id")));

			// ① 마지막 메시지 매핑 (MongoDB 조회)
			ChatMessageDocument lastMsg = chatMessageRepository
					.findFirstByRoomIdOrderByCreatedAtDesc(roomId)
					.orElse(null);
			// React의 chat.lastMessage와 매핑됨
			room.put("lastMessage", lastMsg != null ? lastMsg.getMessage() : "대화 내용이 없습니다.");

			// ② 안읽은 메시지 수 계산
			// Oracle DB에서 유저가 이 방을 마지막으로 읽은 시간 조회
			LocalDateTime lastReadAt = gatheringMapper.selectLastReadAt(roomId, member_id);

			// 만약 읽은 기록이 없다면 가입일(joined_at)을 기준으로 세팅
			if (lastReadAt == null) {
				lastReadAt = gatheringMapper.selectUserJoinedAt(roomId, member_id);
			}

			long unreadCount = 0;
			if (lastReadAt != null) {
				// 마지막 읽은 시간 이후에 생성된 메시지 카운트 (MongoDB 조회)
				unreadCount = chatMessageRepository.countByRoomIdAndCreatedAtGreaterThan(roomId, lastReadAt);
			}
			// React의 chat.unread_count와 매핑됨
			room.put("unread_count", (int) unreadCount);
		}

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

		if (endPage > totalPage)
			endPage = totalPage;
		if (startPage < 1)
			startPage = 1;

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
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(ownerId)) {
			return false;
		}

		gatheringMapper.deleteAllParticipants(roomId);
		return gatheringMapper.deleteGathering(roomId) > 0;
	}

	// 방장 위임
	@Transactional
	public boolean transferHost(Long roomId, String currentOwnerId, String newOwnerId) {
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(currentOwnerId)) {
			return false;
		}

		return gatheringMapper.updateOwner(roomId, newOwnerId) > 0;
	}

	// 참여자 강퇴 (밴 등록 로직 연동)
	@Transactional
	public boolean kickParticipant(Long roomId, String ownerId, String targetMemberId) {
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		if (detail == null || detail.getOwner_id() == null || !detail.getOwner_id().equals(ownerId)) {
			return false;
		}

		if (ownerId.equals(targetMemberId)) {
			return false;
		}

		gatheringMapper.insertBanUser(roomId, targetMemberId);
		return gatheringMapper.deleteParticipant(roomId, targetMemberId) > 0;
	}

	// 유저 밴 여부 확인 (컨트롤러에서 호출)
	public boolean isBanned(Long roomId, String memberId) {
		if (roomId <= 0)
			return false;
		return gatheringMapper.checkBanStatus(roomId, memberId) > 0;
	}

	public GatheringCreateDTO selectGatheringDetail(Long roomId) {
		return gatheringMapper.selectGatheringDetail(roomId);
	}

	public List<Map<String, Object>> getParticipants(Long roomId) {
		if (roomId <= 0)
			return List.of();
		return gatheringMapper.selectParticipants(roomId);
	}

	public List<Map<String, Object>> selectGatheringMembers(Long roomId) {
		if (roomId <= 0)
			return List.of();
		return gatheringMapper.selectGatheringMembers(roomId);
	}

	// 채팅방 실시간 알림 연동 완료된 모임 참여 로직
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
		if (room == null)
			return null;

		int currentCount = Integer.parseInt(room.get("CURRENT_COUNT").toString());
		int maxCapacity = Integer.parseInt(room.get("MAX_CAPACITY").toString());

		if (currentCount >= maxCapacity)
			return null;

		gatheringMapper.insertParticipant(roomId, member_id);

		String nickname = memberDao.selectUserNickname(member_id);
		if (nickname == null)
			nickname = member_id;

		ChatMessageDocument enterMsg = new ChatMessageDocument();
		enterMsg.setRoomId(roomId);
		enterMsg.setSenderId(member_id);
		enterMsg.setSenderName(nickname);
		enterMsg.setMessage(nickname + "님이 입장하셨습니다.");
		enterMsg.setType(com.study.app.domains.chat.ChatType.ENTER);
		enterMsg.setCreatedAt(LocalDateTime.now());

		chatMessageRepository.save(enterMsg);

		messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, enterMsg);

		return roomId;
	}

	// 채팅방 실시간 알림 연동 완료된 모임 퇴장 로직
	@Transactional(rollbackFor = Exception.class)
	public boolean leaveGathering(Long roomId, String member_id) throws Exception {
		if (roomId <= 0)
			return false;

		String nickname = memberDao.selectUserNickname(member_id);
		if (nickname == null)
			nickname = member_id;

		int deletedRows = gatheringMapper.deleteParticipant(roomId, member_id);

		if (deletedRows != 0) {
			ChatMessageDocument leaveMsg = new ChatMessageDocument();
			leaveMsg.setRoomId(roomId);
			leaveMsg.setSenderId(member_id);
			leaveMsg.setSenderName(nickname);
			leaveMsg.setMessage(nickname + "님이 퇴장하셨습니다.");
			leaveMsg.setType(com.study.app.domains.chat.ChatType.LEAVE);
			leaveMsg.setCreatedAt(LocalDateTime.now());

			chatMessageRepository.save(leaveMsg);

			messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, leaveMsg);
		}

		return deletedRows != 0;
	}

}