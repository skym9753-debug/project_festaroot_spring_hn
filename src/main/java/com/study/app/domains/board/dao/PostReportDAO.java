package com.study.app.domains.board.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.board.dto.PostReportDTO;

@Repository
public class PostReportDAO {
	
    @Autowired
    private SqlSessionTemplate mybatis;

    public int countReport(PostReportDTO dto) {
        return mybatis.selectOne("PostReport.countReport", dto);
    }

    public int insertReport(PostReportDTO dto) {
        return mybatis.insert("PostReport.insertReport", dto);
    }

}
