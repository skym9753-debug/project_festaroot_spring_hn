package com.study.app.domains.board.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.study.app.domains.achievement.AchievementService;
import com.study.app.domains.achievement.AchievementService.ActivityType;
import com.study.app.domains.achievement.dto.AchievementResultDTO;
import com.study.app.domains.board.dao.FileDAO;
import com.study.app.domains.board.dao.PostDAO;
import com.study.app.domains.board.dto.CommunityPostDTO;
import com.study.app.domains.board.dto.PostAttachmentDTO;
import com.study.app.domains.storage.UploadService;

@Service
public class BoardService {

	@Autowired
	private PostDAO postDAO;

	@Autowired
	private FileDAO fileDAO;
	
	@Autowired
	private UploadService uploadService;
	
	@Autowired
	private AchievementService achievementService;
	
	@Autowired
	private com.study.app.domains.activity.UserActivityLogService userActivityLogService;
	
	@Transactional
	public List<AchievementResultDTO> addPost(CommunityPostDTO dto, List<MultipartFile> files) {
		postDAO.insertPost(dto);
		
		if(files != null && !files.isEmpty()) {
			fileDAO.insertPostAttachments(dto.getPost_id(), files);	
		}
		
		// Activity log hook
//		com.study.app.domains.activity.dto.UserActivityLogDTO log = new com.study.app.domains.activity.dto.UserActivityLogDTO();
//		log.setMember_id(dto.getMember_id());
//		log.setAction_type("POST_WRITE");
//		log.setContent_id(dto.getPost_id());
//		userActivityLogService.saveLog(log);
		
		return achievementService.addActivityExp(dto.getMember_id(), ActivityType.POST);
	}

	public int totalPostCount(Map<String, Object> params) {
		return postDAO.selectCount(params);
	}

	public List<CommunityPostDTO> getPosts(Map<String, Object> params){
		return postDAO.selectList(params);
	}

	public CommunityPostDTO getPostDetail(Long id) {
		postDAO.increaseViewCount(id);
		CommunityPostDTO post = postDAO.selectById(id);
		
		if (post != null) {
			post.setAttachments(fileDAO.selectPostAttachByPostId(id));
		}
		
		return post;
	}
	
	public List<PostAttachmentDTO> getPostAttachList(Long id) {
		return fileDAO.selectPostAttachByPostId(id);
	}

	public void updatePost(CommunityPostDTO dto, List<MultipartFile> files) {
		postDAO.updatePostById(dto);
		
		if (dto.getDeleteFileIds() != null) {
			for (Long attach_id : dto.getDeleteFileIds()) {
				PostAttachmentDTO attachment = fileDAO.selectPostAttachById(attach_id);
				
				if (attachment != null) {
					fileDAO.deletePostAttachById(attach_id);
				}
			}
		}
		
		System.out.println("Post DTO ID = " + dto.getPost_id());

		if (files != null && !files.isEmpty()) {
			fileDAO.insertPostAttachments(dto.getPost_id(), files);	
		}
	}

	public void deletePost(Long id) {
		
//	    // 0. Load post first if content image cleanup is needed
//	    CommunityPostDTO post = postDAO.selectById(id);
//	    if (post != null) {
//	        // Delete inline images from the content body
//	        uploadService.deleteImagesFromContent(post.getContent());
//	    }
//		
//	    // 1. Load post attachments
//	    List<PostAttachmentDTO> attachments = fileDAO.selectPostAttachByPostId(id);
//
//	    // 2. Delete physical files from GCP Storage
//	    for (PostAttachmentDTO file : attachments) {
//	        if (file.getFile_path() != null) {
//	            uploadService.deleteFile(file.getFile_path());
//	        }
//	    }
//
//	    // 3. Delete attachment rows from DB
		

	    
		postDAO.deletePostById(id);
		
	}
	public Integer getMyPostCount(String memberId) {
		return postDAO.getMyPostCount(memberId);
	}
	public List<CommunityPostDTO> getMypostList(String member_id){
		return postDAO.getMypostList(member_id);
	}

}
