package com.study.app.domains.auth;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.study.app.domains.member.dto.MemberDTO;

@Repository
public class AuthDAO {
    private final SqlSessionTemplate mybatis;

    public AuthDAO(SqlSessionTemplate mybatis) {
        this.mybatis = mybatis;
    }


    public MemberDTO selectMemberById(String member_id) {
        return mybatis.selectOne("Auth.selectMemberById", member_id);
    }

    /*
     * 소셜 로그인용 회원 조회
     */
    public MemberDTO selectMemberBySocialIdAndProvider(String socialId, String socialProvider) {
        Map<String, Object> params = new HashMap<>();
        params.put("socialId", socialId);
        params.put("socialProvider", socialProvider);

        return mybatis.selectOne("Auth.selectMemberBySocialIdAndProvider", params);
    }
    
    // 정지 해제 업데이트 실행 메서드
    public int updateClearSuspension(String member_id) {
        return mybatis.update("Auth.updateClearSuspension", member_id);
    }
    
    // 최근 접속일 업데이트 실행 메서드
    public int updateLastLogin(String member_id) {
        return mybatis.update("Auth.updateLastLogin", member_id);
    }
}
