package com.study.app.domains.board.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.board.dao.CommentActionDAO;

@Service
public class CommentActionService {
	
	 @Autowired
	    private CommentActionDAO commentActionDAO;

	    // 댓글 / 대댓글 좋아요 토글
	    public Map<String, Object> toggleCommentLike(
	            Long comment_id,
	            String member_id
	    ) {
	        int count =
	            commentActionDAO.countCommentLike(comment_id, member_id);

	        boolean liked;

	        if (count > 0) {
	            // 이미 좋아요를 눌렀으면 취소
	            commentActionDAO.deleteCommentLike(comment_id, member_id);
	            commentActionDAO.decreaseCommentLikeCount(comment_id);
	            liked = false;
	        } else {
	            // 좋아요를 누르지 않은 상태면 추가
	            commentActionDAO.insertCommentLike(comment_id, member_id);
	            commentActionDAO.increaseCommentLikeCount(comment_id);
	            liked = true;
	        }

	        Long likeCount =
	            commentActionDAO.getCommentLikeCount(comment_id);

	        Map<String, Object> result = new HashMap<>();
	        result.put("liked", liked);
	        result.put("likeCount", likeCount);

	        return result;
	    }

	    // 댓글 / 대댓글 신고
	    public boolean reportComment(
	            Long comment_id,
	            String member_id,
	            String reason
	    ) {
	        int count =
	            commentActionDAO.countCommentReport(comment_id, member_id);

	        // 이미 신고한 경우
	        if (count > 0) {
	            return false;
	        }

	        int result =
	            commentActionDAO.insertCommentReport(
	                comment_id,
	                member_id,
	                reason
	            );

	        if (result > 0) {
	            commentActionDAO.increaseCommentReportCount(comment_id);
	            return true;
	        }

	        return false;
	    }

}
