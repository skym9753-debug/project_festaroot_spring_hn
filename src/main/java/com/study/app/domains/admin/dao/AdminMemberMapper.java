package com.study.app.domains.admin.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.study.app.domains.admin.dto.AdminMemberDTO;

@Mapper
public interface AdminMemberMapper {

	// 동적 필터링 회원 조회 (페이징 쿼리가 적용됨)
	List<AdminMemberDTO.Response> selectFilteredMembers(AdminMemberDTO.SearchParam params);

	// 페이징 계산을 위한 필터링된 총 회원 수 조회
	long selectFilteredMembersCount(AdminMemberDTO.SearchParam params);

	int updateMemberSuspension(@Param("id") String id, @Param("days") int days);

	int updateMemberBlacklist(@Param("id") String id);

	int updateMemberRestore(@Param("id") String id);

	// 특정 회원의 승인된 신고 내역 리스트 호출
	List<AdminMemberDTO.ReportHistoryResponse> selectAcceptReportsByMemberId(@Param("id") String id);

	// 전체 요약 통계 데이터 조회 (검색 필터링 영향 없음)
	AdminMemberDTO.MainStats selectMainStats();
}