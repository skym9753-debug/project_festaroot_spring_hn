package com.study.app.domains.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.study.app.domains.board.dto.CommunityPostDTO;

@Service
public class BoardService {
	
	@Autowired
	private PostDAO postDAO;
	
	@Autowired
	private FileDAO fileDAO;
	
	public void addPost(CommunityPostDTO dto, List<MultipartFile> files) {
		System.out.println(dto.getPost_id());
		postDAO.insertPost(dto);
		System.out.println(dto.getPost_id());
		Long boardSeq = dto.getPost_id();
		
		fileDAO.insertPostAttachments(boardSeq, files);	
		
	}
	
	public int totalPostCount() {
		return postDAO.selectCount();
	}

}
