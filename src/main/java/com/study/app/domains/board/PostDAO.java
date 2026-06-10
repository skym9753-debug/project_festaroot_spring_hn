package com.study.app.domains.board;

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
	
	public int selectCount() {
		return mybatis.selectOne("Board.selectCount");
	}

}
