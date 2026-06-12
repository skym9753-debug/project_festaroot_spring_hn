package com.study.app.domains.board.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.board.dao.PostLikeDAO;
import com.study.app.domains.board.dto.PostLikeDTO;

@Service
public class PostLikeService {
	
	
    @Autowired
    private PostLikeDAO likeDAO;

    public Map<String, Object> toggleLike(Long post_id, String member_id) {
        PostLikeDTO dto = new PostLikeDTO();
        dto.setPost_id(post_id);
        dto.setMember_id(member_id);

        int count = likeDAO.countLike(dto);

        boolean liked;

        if (count > 0) {
            likeDAO.deleteLike(dto);
            likeDAO.decreaseLikeCount(post_id);
            liked = false;
        } else {
            likeDAO.insertLike(dto);
            likeDAO.increaseLikeCount(post_id);
            liked = true;
        }

        int likeCount = likeDAO.getLikeCount(post_id);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);

        return result;
    }

    public Map<String, Object> getLikeStatus(Long post_id, String member_id) {
        PostLikeDTO dto = new PostLikeDTO();
        dto.setPost_id(post_id);
        dto.setMember_id(member_id);

        boolean liked = likeDAO.countLike(dto) > 0;
        int likeCount = likeDAO.getLikeCount(post_id);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);

        return result;
    }

}
