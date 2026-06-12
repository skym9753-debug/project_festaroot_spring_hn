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
	public List<GatheringCreateDTO> selectGatheringList() {
		List<GatheringCreateDTO> result = gatheringMapper.selectGatheringList();
		return result;
	}

	// 자유 모임 상세 조회
	public GatheringCreateDTO selectGatheringDetail(Long roomId) {
		return gatheringMapper.selectGatheringDetail(roomId);
	}

	// 자유 모임 참여자 목록 상세 조회
	public List<Map<String, Object>> getParticipants(Long roomId) {
		return gatheringMapper.selectParticipants(roomId);
	}

	// 모임 참여
	@Transactional(rollbackFor = Exception.class)
	public boolean joinGathering(Long roomId, String memberId) throws Exception {
		// 1. 현재 인원 및 최대 정원 실시간 확인
		Map<String, Object> room = gatheringMapper.getRoomCapacityInfo(roomId);
		if (room == null)
			return false;

		// Oracle Map 반환값 특성상 대문자 Key 사용
		int currentCount = Integer.parseInt(room.get("CURRENT_COUNT").toString());
		int maxCapacity = Integer.parseInt(room.get("MAX_CAPACITY").toString());

		// 정원 초과 검증
		if (currentCount >= maxCapacity) {
			return false;
		}

		// 2. 참여자 테이블(CHAT_ROOM_USER)에 세션 유저 추가
		gatheringMapper.insertParticipant(roomId, memberId);

		return true;
	}

	// 모임 탈퇴
	@Transactional(rollbackFor = Exception.class)
	public boolean leaveGathering(Long roomId, String memberId) throws Exception {
		// 1. 참여자 테이블에서 행 제거
		int deletedRows = gatheringMapper.deleteParticipant(roomId, memberId);
		if (deletedRows == 0) {
			return false; // 지워진 데이터가 없음 (애초에 참여자가 아님)
		}

		return true;
	}

}
