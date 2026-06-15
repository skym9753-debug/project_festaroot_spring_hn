package com.study.app.domains.api;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TermsDAO {
	
	@Autowired
    private SqlSessionTemplate mybatis;

    public List<TermsDTO> selectActiveTerms() {
        return mybatis.selectList("Api.selectActiveTerms");
    }

}
