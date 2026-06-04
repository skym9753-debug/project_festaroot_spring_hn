package com.study.app.domains.member;

import org.apache.ibatis.annotations.Mapper;

import com.study.app.domains.member.dto.InterestRegionDTO;
import com.study.app.domains.member.dto.InterestThemeDTO;
import com.study.app.domains.member.dto.MemberDTO;

@Mapper
public interface MemberDAO {
    int insertMember(MemberDTO memberDTO);
    
    int insertInterestTheme(InterestThemeDTO dto);

    int insertInterestRegion(InterestRegionDTO dto);

}
