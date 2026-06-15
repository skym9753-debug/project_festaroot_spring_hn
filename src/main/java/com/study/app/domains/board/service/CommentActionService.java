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

	 @Autowired
	    private com.study.app.domains.board.dao.PostCommentDAO postCommentDAO;

	 @Autowired
	    private com.study.app.domains.achievement.AchievementService achievementService;

	 @Autowired
	    private com.study.app.domains.member.MemberDAO memberDAO;

	 @Autowired
	    private com.study.app.domains.notification.NotificationService notificationService;

	    // 댓글 / 대댓글 좋아요 토글
	    public Map<String, Object> toggleCommentLike(
	            Long comment_id,
	            String member_id
	    ) {
	        int count =
	            commentActionDAO.countCommentLike(comment_id, member_id);

	        boolean liked;
	        java.util.List<com.study.app.domains.achievement.dto.AchievementResultDTO> achievements = null;

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

                // 1. 좋아요 누른 사람 보상
                achievements = achievementService.addActivityExp(member_id, com.study.app.domains.achievement.AchievementService.ActivityType.LIKE_GIVEN);

                // 2. 좋아요 받은 사람 보상 및 알림
                com.study.app.domains.board.dto.PostCommentDTO comment = postCommentDAO.selectCommentById(comment_id);
                if (comment != null && !member_id.equals(comment.getMember_id())) {
                    // 업적 연동
                    achievementService.addActivityExp(comment.getMember_id(), com.study.app.domains.achievement.AchievementService.ActivityType.RECEIVE_LIKE);

                    // 실시간 좋아요 알림 생성
                    com.study.app.domains.member.dto.MemberDTO liker = memberDAO.selectMemberById(member_id);
                    String likerNickname = (liker != null) ? liker.getNickname() : "누군가";
                    
                    // 댓글 내용이 너무 길면 자름
                    String commentSnippet = comment.getContent();
                    if (commentSnippet.length() > 20) {
                        commentSnippet = commentSnippet.substring(0, 17) + "...";
                    }

                    com.study.app.domains.notification.dto.NotificationDTO noti = new com.study.app.domains.notification.dto.NotificationDTO();
                    noti.setMember_id(comment.getMember_id());
                    noti.setNoti_type("LIKE");
                    noti.setReference_id(comment.getPost_id()); // 댓글 클릭 시 게시글로 이동할 수 있도록 post_id 저장
                    noti.setContent(likerNickname + "님이 회원님의 댓글 \"" + commentSnippet + "\"을 좋아합니다.");
                    notificationService.saveNotification(noti);
                }
	        }

	        Long likeCount =
	            commentActionDAO.getCommentLikeCount(comment_id);

	        Map<String, Object> result = new HashMap<>();
	        result.put("liked", liked);
	        result.put("likeCount", likeCount);
            if (achievements != null) {
                result.put("achievements", achievements);
            }

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
