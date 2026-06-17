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
import com.study.app.domains.admin.service.AdminMemberService;

@RestController
@RequestMapping("/admin/members")
public class AdminMemberController {

	private final AdminMemberService adminMemberService;

	public AdminMemberController(AdminMemberService adminMemberService) {
		super();
		this.adminMemberService = adminMemberService;
	}

	// 조건별 회원 목록 조회
	@GetMapping("") // 빈 문자열로 매핑하여 기본 경로 수신
	public ResponseEntity<List<AdminMemberDTO.Response>> getMembers(AdminMemberDTO.SearchParam params) {
		System.out.println("수신된 검색 필터 조건: " + params.toString());

		// TODO: 서비스 단으로 파라미터를 넘겨 동적 쿼리(MyBatis 혹은 JPA Dynamic Query)로 필터링 조회 진행
		List<AdminMemberDTO.Response> list = adminMemberService.findFilteredMembers(params);

		return ResponseEntity.ok(list);
	}

	// 회원 기간 제한 정지
	@PutMapping("/{id}/suspend")
	public ResponseEntity<String> suspendMember(@PathVariable("id") String id,
			@RequestBody AdminMemberDTO.SuspendRequest request) {

		System.out.println("정지 대상 회원 ID: " + id + ", 정지 일수: " + request.getSuspensionDays());

		// TODO: 서비스 단에서 회원 상태를 'SUSPENDED'로 바꾸고, 현재 날짜 기반으로 제재 종료일 계산하여 DB Update
		adminMemberService.suspendMember(id, request.getSuspensionDays());

		return ResponseEntity.ok("회원 정지 처리가 성공적으로 반영되었습니다.");
	}

	// 회원 블랙리스트 영구 정지
	@PutMapping("/{id}/blacklist")
	public ResponseEntity<String> blacklistMember(@PathVariable("id") String id) {
		System.out.println("블랙리스트 등록 대상 회원 ID: " + id);

		// TODO: 회원 상태를 'BLACKLISTED'로 바꾸고 제재 기간을 '영구'로 DB 세팅
		adminMemberService.blacklistMember(id);

		return ResponseEntity.ok("해당 회원이 블랙리스트에 등록되었습니다.");
	}

	// 회원 제재 해제 복원
	@PutMapping("/{id}/restore")
	public ResponseEntity<String> restoreMember(@PathVariable("id") String id) {
		System.out.println("제재 해제 대상 회원 ID: " + id);

		// TODO: 회원 상태를 'ACTIVE'로 초기화하고 제재 내역 클리어 처리
		adminMemberService.restoreMember(id);

		return ResponseEntity.ok("회원의 제재 상태가 정상적으로 해제되었습니다.");
	}

}
