package com.study.app.domains.gathering;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GatheringMapper {

	// 새 모임/채팅방 개설
	int insertGathering(GatheringCreateDTO gathering);

	// 채팅방 참여자 등록 (방장 자동 참여용)
	int insertRoomUser(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 자유 모임 목록 조회 (페이징 및 검색 지원)
	List<Map<String, Object>> selectGatheringList(@Param("member_id") String memberId, @Param("page") int page,
			@Param("size") int size, @Param("keyword") String keyword);

	// 자유 모임 총 개수 조회 (검색 지원)
	int countGatheringList(@Param("keyword") String keyword);

	// 자유 모임 및 축제 모임 통합 상세 조회 (음수/양수 대응 고도화)
	GatheringCreateDTO selectGatheringDetail(@Param("roomId") Long roomId);

	// 채팅 참여자 목록 상세 조회
	List<Map<String, Object>> selectParticipants(@Param("room_id") Long roomId);

	// 모임 참여자 목록 상세 조회(방장 중복 제외)
	List<Map<String, Object>> selectGatheringMembers(@Param("room_id") Long roodId);

	// 모임 수정
	int updateGathering(GatheringCreateDTO gathering);

	// 모임 삭제
	int deleteGathering(@Param("room_id") Long roomId);

	// 모임 참여자 전체 삭제 (방 삭제 전처리)
	int deleteAllParticipants(@Param("room_id") Long roomId);

	// 방장 위임
	int updateOwner(@Param("room_id") Long roomId, @Param("new_owner_id") String newOwnerId);

	// 모임 정원 및 현재 인원 조회
	Map<String, Object> getRoomCapacityInfo(@Param("room_id") Long roomId);

	// 모임 참여자 등록
	int insertParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 모임 탈퇴 (참여자 제거)
	int deleteParticipant(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 축제 모임 전체 목록 조회 (축제 전체 기준 + 채팅방 조인) (페이징 및 검색 지원)
	List<Map<String, Object>> selectFestivalGatheringList(@Param("member_id") String member_id, @Param("page") int page,
			@Param("size") int size, @Param("keyword") String keyword);

	// 축제 모임 총 개수 조회 (검색 지원)
	int countFestivalGatheringList(@Param("keyword") String keyword);

	// 참여중인 모임 목록 (페이징 및 검색 지원)
	List<Map<String, Object>> selectJoinedGatheringList(@Param("member_id") String member_id, @Param("page") int page,
			@Param("size") int size, @Param("filter") String filter, @Param("keyword") String keyword);

	// 참여중인 모임 총 개수 조회 (검색 지원)
	int countJoinedGatheringList(@Param("member_id") String member_id, @Param("filter") String filter,
			@Param("keyword") String keyword);

	// 밴 여부 확인 (카운트가 1 이상이면 밴 유저)
	int checkBanStatus(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 강퇴 시 밴 유저 리스트에 등록
	int insertBanUser(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 채팅방 입장 시간 추적
	LocalDateTime selectUserJoinedAt(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 마지막으로 보낸 메세지 추적
	LocalDateTime selectLastReadAt(@Param("room_id") Long roomId, @Param("member_id") String memberId);

	// 마지막으로 읽은 메세지 추적
	int updateLastReadAt(
	    @Param("room_id") Long roomId, 
	    @Param("member_id") String memberId, 
	    @Param("lastReadAt") java.time.LocalDateTime lastReadAt
	);
	
	// 인기 모임 목록
	List<PopularGatheringDTO> getPopularGatherings();
	
	int deleteParticipantFromAllRooms(@Param("member_id") String memberId);
	
	// 모임 신고 등록
	int insertReport(@Param("room_id") Long roomId, @Param("reporter_id") String reporterId, @Param("report_reason") String reportReason);

	// 관리자 모임 목록 조회
	List<Map<String, Object>> selectAdminGatherings(Map<String, Object> params);

	// 관리자 모임 개수 조회
	int countAdminGatherings(Map<String, Object> params);

	// 모임 상태 변경
	int updateGatheringStatus(@Param("roomId") Long roomId, @Param("status") String status);

	// 특정 모임의 신고 내역 조회
	List<Map<String, Object>> selectReportsByRoomId(@Param("roomId") Long roomId);

	// 신고 이력의 최대 ID 조회
	long selectMaxHistoryId();

	// 신고 이력 등록
	int insertReportHistory(@Param("historyId") Long historyId, @Param("targetMemberId") String targetMemberId,
			@Param("reporterId") String reporterId, @Param("roomId") Long roomId, @Param("reportId") Long reportId,
			@Param("reason") String reason, @Param("resultStatus") String resultStatus, @Param("adminMemo") String adminMemo);

	// 모임에 대한 이력 존재 여부 확인
	int checkHistoryExists(@Param("roomId") Long roomId);

	int updateReportHistoryMemo(@Param("roomId") Long roomId, @Param("adminMemo") String adminMemo);

	// 동일 모임 중복 신고 개수 조회
	int selectReportCountByReporter(@Param("roomId") Long roomId, @Param("reporterId") String reporterId);

	// 특정 모임의 모든 신고 내역 삭제
	int deleteReportsByRoomId(@Param("roomId") Long roomId);

	// 개별 신고 상세 조회
	Map<String, Object> selectReportById(@Param("reportId") Long reportId);

	// 개별 신고 삭제
	int deleteReportById(@Param("reportId") Long reportId);

	// 신고 누적 집중 모니터링 모임 조회 (3회 이상)
	List<Map<String, Object>> selectCautionGatherings();

}