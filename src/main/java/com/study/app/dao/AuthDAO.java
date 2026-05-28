package com.study.app.dao;

import org.apache.ibatis.annotations.Mapper;

import com.study.app.dto.LoginDTO;
import com.study.app.dto.MemberDTO;

@Mapper
public interface AuthDAO {
    MemberDTO selectMemberById(String member_id);
}
