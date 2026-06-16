package com.study.app.domains.member;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.member.dto.FindIdRequestDTO;
import com.study.app.domains.member.dto.InterestRegionDTO;
import com.study.app.domains.member.dto.InterestThemeDTO;
import com.study.app.domains.member.dto.MemberDTO;
import com.study.app.domains.member.dto.PasswordFindRequestDTO;
import com.study.app.domains.member.dto.UpdatePasswordDTO;

@Repository
public class MemberDAO {

	@Autowired
	private SqlSessionTemplate mybatis;

	public MemberDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}

	// 회원가입
	public int insertMember(MemberDTO memberDTO) {
		return mybatis.insert("Member.insertMember", memberDTO);
	}

	// 아이디로 회원 조회
	public MemberDTO selectMemberById(String member_id) {
		return mybatis.selectOne("Member.selectMemberById", member_id);
	}

	// 관심 지역 조회
	public List<InterestRegionDTO> selectInterestRegions(String member_id) {
		return mybatis.selectList("Member.selectInterestRegions", member_id);
	}

	// 관심 테마 조회
	public List<InterestThemeDTO> selectInterestThemes(String member_id) {
		return mybatis.selectList("Member.selectInterestThemes", member_id);
	}

	// 관심 테마 등록
	public int insertInterestTheme(InterestThemeDTO dto) {
		return mybatis.insert("Member.insertInterestTheme", dto);
	}

	// 관심 지역 등록
	public int insertInterestRegion(InterestRegionDTO dto) {
		return mybatis.insert("Member.insertInterestRegion", dto);
	}

	// 프로필 조회
	public MemberDTO getProfile(String member_id) {
		return mybatis.selectOne("Member.getProfile", member_id);
	}

	// 회원 정보 수정
	public int updateMember(MemberDTO memberDTO) {
		return mybatis.update("Member.updateMember", memberDTO);
	}

	// 기존 관심 테마 삭제
	public int deleteInterestThemes(String member_id) {
		return mybatis.delete("Member.deleteInterestThemes", member_id);
	}

	// 기존 관심 지역 삭제
	public int deleteInterestRegions(String member_id) {
		return mybatis.delete("Member.deleteInterestRegions", member_id);
	}

	// 아이디 중복 체크
	public int countByMemberId(String member_id) {
		return mybatis.selectOne("Member.countByMemberId", member_id);
	}

	// 닉네임 중복 체크
	public int countByNickname(String nickname) {
		return mybatis.selectOne("Member.countByNickname", nickname);
	}

	// 이메일 중복 체크
	public int existsByEmail(String email) {
		return mybatis.selectOne("Member.existsByEmail", email);
	}

	public String findIdByNameAndEmail(FindIdRequestDTO dto) {
		return mybatis.selectOne("Member.findIdByNameAndEmail", dto);
	}

	public int existsForPasswordReset(PasswordFindRequestDTO dto) {
		return mybatis.selectOne("Member.existsForPasswordReset", dto);
	}

	public int updatePassword(Map<String, Object> params) {
		return mybatis.update("Member.updatePassword", params);
	}

	public int withdrawMember(String member_id) {
		return mybatis.update("Member.withdrawMember", member_id);
	}

	// member_id 기준으로 닉네임 조회
	public String selectUserNickname(@Param("member_id") String memberId) {
		return mybatis.selectOne("Member.selectUserNickname", memberId);
	};
}
