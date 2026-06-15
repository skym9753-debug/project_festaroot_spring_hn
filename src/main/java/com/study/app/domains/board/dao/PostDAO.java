package com.study.app.domains.board.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.cloud.storage.Storage;
import com.study.app.domains.board.dto.CommunityPostDTO;

@Repository
public class PostDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
	@Autowired
	private Storage storage;
	
	public void insertPost(CommunityPostDTO dto) {
		mybatis.insert("Board.insertPost", dto);
	}
	
	public int selectCount(Map<String, Object> params) {
		return mybatis.selectOne("Board.selectCount", params);
	}
	
	public List<CommunityPostDTO> selectList(Map<String, Object> params) {		
		return mybatis.selectList("Board.selectList", params);
	}
	
	public CommunityPostDTO selectById(Long id) {
		return mybatis.selectOne("Board.selectById", id);
	}
	
	public void updatePostById(CommunityPostDTO dto) {
		mybatis.update("Board.updatePostById", dto);
	}
	
	public int increaseViewCount(Long post_id) {
	    return mybatis.update("Board.increaseViewCount", post_id);
	}
	public void deletePostById(Long id) {
		mybatis.delete("Board.deletePostById", id);
	}
	
	public int increaseReportCount(Long post_id) {
		return mybatis.update("Board.increaseReportCount", post_id);
	}

}
