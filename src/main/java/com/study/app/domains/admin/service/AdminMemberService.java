package com.study.app.domains.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.admin.dao.AdminMemberMapper;
import com.study.app.domains.admin.dto.AdminMemberDTO;

@Service
@Transactional // 제어/수정 작업이 많으므로 트랜잭션 어노테이션 추가
public class AdminMemberService {

    private final AdminMemberMapper adminMemberMapper;

    public AdminMemberService(AdminMemberMapper adminMemberMapper) {
        this.adminMemberMapper = adminMemberMapper;
    }

    // 조건별 회원 목록 조회
    @Transactional(readOnly = true)
    public List<AdminMemberDTO.Response> findFilteredMembers(AdminMemberDTO.SearchParam params) {
        return adminMemberMapper.selectFilteredMembers(params);
    }

    // 회원 기간 제한 정지 (상태를 SUSPENDED로 변경 및 정지 기한 일수 계산)
    public void suspendMember(String id, int suspensionDays) {
        adminMemberMapper.updateMemberSuspension(id, suspensionDays);
    }

    // 회원 블랙리스트 영구 정지 (상태를 BLACKLISTED로 변경)
    public void blacklistMember(String id) {
        adminMemberMapper.updateMemberBlacklist(id);
    }

    // 회원 제재 해제 복원 (상태를 ACTIVE로 복구 및 정지 기한 초기화)
    public void restoreMember(String id) {
        adminMemberMapper.updateMemberRestore(id);
    }
}