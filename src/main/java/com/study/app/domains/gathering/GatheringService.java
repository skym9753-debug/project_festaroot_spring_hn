package com.study.app.domains.gathering;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.chat.ChatMessageRepository;
import com.study.app.domains.chat.dto.ChatMessageDocument;
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
			ChatMessageDocument lastMsg = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId)
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
				// 마지막 읽은 시간 이후 + '내가 보내지 않은(SenderIdNot)' 메시지만 카운트
				unreadCount = chatMessageRepository.countByRoomIdAndCreatedAtGreaterThanAndSenderIdNot(roomId,
						lastReadAt, member_id);
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

		if (currentCount + 1 >= maxCapacity) {
			gatheringMapper.updateGatheringStatus(roomId, "HIDDEN");
			System.out.println("====== AUTO-HIDE: Room " + roomId + " is now FULL (" + (currentCount + 1) + "/" + maxCapacity + "). Status updated to HIDDEN. ======");
		}

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

			// 해당 방의 현재 남아있는 인원 수 체크
			Map<String, Object> roomInfo = gatheringMapper.getRoomCapacityInfo(roomId);
			if (roomInfo != null) {
				int currentCount = Integer.parseInt(roomInfo.get("CURRENT_COUNT").toString());

				// 참여자 카운트가 0명이면 CHAT_ROOM 테이블에서 해당 방 완전 자동 삭제
				if (currentCount == 0) {
					gatheringMapper.deleteGathering(roomId);
					return true; // 인원이 없어 방이 정리되었으므로 종료
				}
			}

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

	// 인기 모임 목록
	public List<PopularGatheringDTO> getPopularGatherings() {
		return gatheringMapper.getPopularGatherings();
	}

	// 모임 신고하기
	@Transactional(rollbackFor = Exception.class)
	public boolean reportGathering(Long roomId, String reporterId, String reportReason) {
		int duplicateCount = gatheringMapper.selectReportCountByReporter(roomId, reporterId);
		if (duplicateCount > 0) {
			throw new IllegalStateException("ALREADY_REPORTED");
		}
		return gatheringMapper.insertReport(roomId, reporterId, reportReason) > 0;
	}

	// 관리자용 모임 목록 조회 (페이징 및 필터 지원)
	public Map<String, Object> getAdminGatherings(String status, String keyword, String sortBy, boolean reportedOnly, int page, int size) {
		int startRow = (page - 1) * size + 1;
		int endRow = page * size;

		Map<String, Object> params = new HashMap<>();
		params.put("status", status);
		params.put("keyword", keyword == null ? "" : keyword.trim());
		params.put("sortBy", sortBy);
		params.put("reportedOnly", reportedOnly);
		params.put("startRow", startRow);
		params.put("endRow", endRow);

		List<Map<String, Object>> list = gatheringMapper.selectAdminGatherings(params);
		int totalElements = gatheringMapper.countAdminGatherings(params);
		int totalPages = (int) Math.ceil((double) totalElements / size);

		Map<String, Object> result = new HashMap<>();
		result.put("list", list);
		result.put("totalPages", totalPages);
		result.put("totalElements", totalElements);
		return result;
	}

	// 특정 모임의 신고 목록 조회
	public List<Map<String, Object>> getReportsByRoomId(Long roomId) {
		return gatheringMapper.selectReportsByRoomId(roomId);
	}

	// 특정 모임의 선택된 신고 내역 삭제 (신고 반려)
	@Transactional(rollbackFor = Exception.class)
	public boolean rejectGatheringReports(List<Long> reportIds) {
		if (reportIds != null && !reportIds.isEmpty()) {
			for (Long reportId : reportIds) {
				gatheringMapper.deleteReportById(reportId);
			}
		}
		return true;
	}

	// 관리자용 모임 노출 상태 변경 (ACTIVE, HIDDEN)
	@Transactional(rollbackFor = Exception.class)
	public boolean updateGatheringStatus(Long roomId, String status) {
		return gatheringMapper.updateGatheringStatus(roomId, status) > 0;
	}

	// 관리자 모임 영구 삭제
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteGatheringByAdmin(Long roomId) {
		gatheringMapper.deleteAllParticipants(roomId);
		return gatheringMapper.deleteGathering(roomId) > 0;
	}

	// 관리자용 모임 신고 승인 (BLIND 상태로 전환 및 선택된 회원 신고 이력 적재)
	@Transactional(rollbackFor = Exception.class)
	public boolean acceptGatheringReports(Long roomId, List<Long> reportIds, String adminMemo) {
		// 1. 방 상태를 BLIND로 전환
		gatheringMapper.updateGatheringStatus(roomId, "BLIND");

		// 2. 해당 방의 상세 정보를 가져와 방장 ID 확인
		GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
		String targetMemberId = detail != null ? detail.getOwner_id() : null;

		// 3. 선택된 신고 상세 내역 루프 돌며 MEMBER_REPORT_HISTORY에 이력 적재 및 신고 개별 삭제
		if (reportIds != null && !reportIds.isEmpty()) {
			long currentMaxId = gatheringMapper.selectMaxHistoryId();
			for (Long reportId : reportIds) {
				Map<String, Object> report = gatheringMapper.selectReportById(reportId);
				if (report != null) {
					currentMaxId++;
					String reporterId = report.get("reporter_id").toString();
					String reason = report.get("report_reason").toString();

					// targetMemberId가 존재할 때만 이력 등록 (축제 모임 등 owner_id가 null인 경우 방어)
					if (targetMemberId != null) {
						gatheringMapper.insertReportHistory(
								currentMaxId,
								targetMemberId,
								reporterId,
								roomId,
								reportId,
								reason,
								"ACCEPTED",
								adminMemo
						);
					}
				}
				// 처리 완료된 신고 내역 삭제
				gatheringMapper.deleteReportById(reportId);
			}
		}

		return true;
	}

	// 관리자용 임시 메모 저장 (신고 승인 이전 대기 메모 처리 등)
	@Transactional(rollbackFor = Exception.class)
	public boolean saveGatheringAdminMemo(Long roomId, String adminMemo) {
		int historyCount = gatheringMapper.checkHistoryExists(roomId);
		if (historyCount > 0) {
			// 이미 이력이 있으면 메모를 업데이트
			return gatheringMapper.updateReportHistoryMemo(roomId, adminMemo) > 0;
		} else {
			// 이력이 없으면 임시 WAITING 상태의 이력을 최초 1개 생성하여 메모 저장
			GatheringCreateDTO detail = gatheringMapper.selectGatheringDetail(roomId);
			String targetMemberId = detail != null ? detail.getOwner_id() : null;
			if (targetMemberId == null) {
				return false;
			}
			long nextId = gatheringMapper.selectMaxHistoryId() + 1;
			// 임시 등록이므로 reporterId는 NULL, reportId도 NULL
			return gatheringMapper.insertReportHistory(
					nextId,
					targetMemberId,
					null, // 임시
					roomId,
					null, // 임시
					"관리자 임시 메모 등록",
					"WAITING",
					adminMemo
			) > 0;
		}
	}

	// 신고 누적 집중 모니터링 모임 조회 (3회 이상)
	public List<Map<String, Object>> getCautionGatherings() {
		return gatheringMapper.selectCautionGatherings();
	}

}