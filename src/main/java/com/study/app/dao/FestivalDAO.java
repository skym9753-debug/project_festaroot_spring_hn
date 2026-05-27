package com.study.app.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.dto.FestivalDTO;

@Repository
public class FestivalDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
	public List<FestivalDTO> getAllFestival(){
		return mybatis.selectList("Festival.getAll");
		
	}
	public FestivalDTO selectByContentId(String contentId) {
		return mybatis.selectOne("Festival.selectByContentId",contentId);
	}
	
}
