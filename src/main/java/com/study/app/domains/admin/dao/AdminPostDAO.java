package com.study.app.domains.admin.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AdminPostDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
    // mapper XML의 namespace
    private static final String NAMESPACE = "AdminPost";
    


}
