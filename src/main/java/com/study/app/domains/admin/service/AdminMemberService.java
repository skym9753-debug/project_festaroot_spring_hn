package com.study.app.domains.admin.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.admin.dao.AdminMemberMapper;
import com.study.app.domains.admin.dto.AdminMemberDTO;
import com.study.app.domains.admin.dto.AdminPageResponseDTO;

@Service
@Transactional
public class AdminMemberService {

	private final AdminMemberMapper adminMemberMapper;

	// 생성자 주입
	public AdminMemberService(AdminMemberMapper adminMemberMapper) {
		this.adminMemberMapper = adminMemberMapper;
	}

	// 조건별 회원 목록 조회 (페이징 연산 포함)
	@Transactional(readOnly = true)
	public AdminPageResponseDTO findFilteredMembers(AdminMemberDTO.SearchParam params) {
		// 1. 페이징 처리된 필터링 목록 조회
		List<AdminMemberDTO.Response> memberList = adminMemberMapper.selectFilteredMembers(params);

		// 2. 검색 조건에 맞는 전체 데이터 개수 조회 (네비게이터 계산용)
		long totalElements = adminMemberMapper.selectFilteredMembersCount(params);

		// 3. 전체 페이지 수 계산 (올림 처리)
		int totalPages = (int) Math.ceil((double) totalElements / params.getSize());
		if (totalPages == 0)
			totalPages = 1; // 데이터가 하나도 없을 때도 기본 1페이지로 설정

		// 4. 프론트엔드가 요구하는 포맷으로 패키징하여 반환
		return new AdminPageResponseDTO(memberList, totalPages, totalElements, params.getPage());
	}

	// 회원 기간 제한 정지 처리
	public void suspendMember(String id, int days) {
		adminMemberMapper.updateMemberSuspension(id, days);
	}

	// 회원 블랙리스트 영구 정지 처리
	public void blacklistMember(String id) {
		adminMemberMapper.updateMemberBlacklist(id);
	}

	// 회원 제재 해제 및 복원 처리
	public void restoreMember(String id) {
		adminMemberMapper.updateMemberRestore(id);
	}
	
	// 특정 회원의 신고 승인 내역 조회
	public List<AdminMemberDTO.ReportHistoryResponse> findAcceptReportsByMemberId(String id) {
        return adminMemberMapper.selectAcceptReportsByMemberId(id);
    }
}