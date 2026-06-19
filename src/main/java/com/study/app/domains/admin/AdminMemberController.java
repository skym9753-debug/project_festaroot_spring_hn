package com.study.app.domains.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.dto.AdminMemberDTO;
import com.study.app.domains.admin.dto.AdminPageResponseDTO;
import com.study.app.domains.admin.service.AdminMemberService;

@RestController
@RequestMapping("/admin/members")
public class AdminMemberController {

	private final AdminMemberService adminMemberService;

	public AdminMemberController(AdminMemberService adminMemberService) {
		this.adminMemberService = adminMemberService;
	}

	// 조건별 회원 목록 조회 (페이징 객체 반환하도록 수정)
	@GetMapping("")
	public ResponseEntity<AdminPageResponseDTO> getMembers(AdminMemberDTO.SearchParam params) {
//		System.out.println("수신된 검색 필터 조건: " + params.toString());

		// 서비스 단에서 목록과 토탈 카운트를 연산하여 DTO로 묶어 반환받음
		AdminPageResponseDTO response = adminMemberService.findFilteredMembers(params);

		return ResponseEntity.ok(response);
	}

	// 회원 기간 제한 정지
	@PutMapping("/{id}/suspend")
	public ResponseEntity<String> suspendMember(@PathVariable("id") String id,
			@RequestBody AdminMemberDTO.SuspendRequest request) {
		System.out.println("정지 대상 회원 ID: " + id + ", 정지 일수: " + request.getSuspensionDays());
		adminMemberService.suspendMember(id, request.getSuspensionDays());
		return ResponseEntity.ok("회원 정지 처리가 성공적으로 반영되었습니다.");
	}

	// 회원 블랙리스트 영구 정지
	@PutMapping("/{id}/blacklist")
	public ResponseEntity<String> blacklistMember(@PathVariable("id") String id) {
		System.out.println("블랙리스트 등록 대상 회원 ID: " + id);
		adminMemberService.blacklistMember(id);
		return ResponseEntity.ok("해당 회원이 블랙리스트에 등록되었습니다.");
	}

	// 회원 제재 해제 복원
	@PutMapping("/{id}/restore")
	public ResponseEntity<String> restoreMember(@PathVariable("id") String id) {
		System.out.println("제재 해제 대상 회원 ID: " + id);
		adminMemberService.restoreMember(id);
		return ResponseEntity.ok("회원의 제재 상태가 정상적으로 해제되었습니다.");
	}

	// 특정 회원의 승인된 신고 이력 상세 조회 API
	@GetMapping("/{id}/reports")
	public ResponseEntity<List<AdminMemberDTO.ReportHistoryResponse>> getMemberReportHistory(
			@PathVariable("id") String id) {
		System.out.println("신고 내역 증거 조회 대상 회원 ID: " + id);

		List<AdminMemberDTO.ReportHistoryResponse> historyList = adminMemberService.findAcceptReportsByMemberId(id);

		return ResponseEntity.ok(historyList);
	}

	// 상단 요약 통계 조회 (검색 조건에 영향 받지 않음)
	@GetMapping("/stats")
	public ResponseEntity<AdminMemberDTO.MainStats> getMainStats() {
		return ResponseEntity.ok(adminMemberService.getMainStats());
	}

}
