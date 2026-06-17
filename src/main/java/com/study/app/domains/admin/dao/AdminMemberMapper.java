package com.study.app.domains.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.study.app.domains.admin.dto.AdminMemberDTO;

@Mapper
public interface AdminMemberMapper {

    // 동적 필터링 회원 조회
    List<AdminMemberDTO.Response> selectFilteredMembers(AdminMemberDTO.SearchParam params);

    // 기간 제한 정지 업뎨이트
    int updateMemberSuspension(@Param("id") String id, @Param("days") int days);

    // 블랙리스트 업데이트
    int updateMemberBlacklist(@Param("id") String id);

    // 제재 해제 복원 업데이트
    int updateMemberRestore(@Param("id") String id);
}
