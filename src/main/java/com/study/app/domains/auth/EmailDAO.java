package com.study.app.domains.auth;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.auth.dto.EmailVerificationDTO;

@Repository
public class EmailDAO {
	
	@Autowired
    private SqlSessionTemplate mybatis;

    public EmailVerificationDTO selectByEmail(String email) {
        return mybatis.selectOne("Email.selectByEmail", email);
    }

    public int insertCode(EmailVerificationDTO dto) {
        return mybatis.insert("Email.insertCode", dto);
    }

    public int updateCode(EmailVerificationDTO dto) {
        return mybatis.update("Email.updateCode", dto);
    }

    public int verifyEmail(String email) {
        return mybatis.update("Email.verifyEmail", email);
    }

    public boolean isEmailVerified(String email) {
        int count = mybatis.selectOne("Email.isEmailVerified", email);
        return count > 0;
    }

}
