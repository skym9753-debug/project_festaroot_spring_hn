package com.study.app.dao;

import org.apache.ibatis.annotations.Mapper;
import com.study.app.dto.MemberDTO;

@Mapper
public interface MemberDAO {
    int insertMember(MemberDTO memberDTO);

}
