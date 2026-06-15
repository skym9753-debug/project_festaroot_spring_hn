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

    @Autowired
    private com.study.app.domains.board.dao.PostDAO postDAO;

    @Autowired
    private com.study.app.domains.achievement.AchievementService achievementService;

    @Autowired
    private com.study.app.domains.member.MemberDAO memberDAO;

    @Autowired
    private com.study.app.domains.notification.NotificationService notificationService;

    public Map<String, Object> toggleLike(Long post_id, String member_id) {
        PostLikeDTO dto = new PostLikeDTO();
        dto.setPost_id(post_id);
        dto.setMember_id(member_id);

        int count = likeDAO.countLike(dto);

        boolean liked;
        java.util.List<com.study.app.domains.achievement.dto.AchievementResultDTO> achievements = null;

        if (count > 0) {
            likeDAO.deleteLike(dto);
            likeDAO.decreaseLikeCount(post_id);
            liked = false;
        } else {
            likeDAO.insertLike(dto);
            likeDAO.increaseLikeCount(post_id);
            liked = true;

            // 1. 좋아요 누른 사람 보상 (인터셉터용)
            achievements = achievementService.addActivityExp(member_id, com.study.app.domains.achievement.AchievementService.ActivityType.LIKE_GIVEN);

            // 2. 좋아요 받은 사람 보상 및 알림
            com.study.app.domains.board.dto.CommunityPostDTO post = postDAO.selectById(post_id);
            if (post != null && !member_id.equals(post.getMember_id())) {
                // 업적 연동
                achievementService.addActivityExp(post.getMember_id(), com.study.app.domains.achievement.AchievementService.ActivityType.RECEIVE_LIKE);

                // 실시간 좋아요 알림 생성
                com.study.app.domains.member.dto.MemberDTO liker = memberDAO.selectMemberById(member_id);
                String likerNickname = (liker != null) ? liker.getNickname() : "누군가";
                
                com.study.app.domains.notification.dto.NotificationDTO noti = new com.study.app.domains.notification.dto.NotificationDTO();
                noti.setMember_id(post.getMember_id());
                noti.setNoti_type("LIKE");
                noti.setReference_id(post_id);
                noti.setContent(likerNickname + "님이 회원님의 게시글 \"" + post.getTitle() + "\"을 좋아합니다.");
                notificationService.saveNotification(noti);
            }
        }

        int likeCount = likeDAO.getLikeCount(post_id);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        if (achievements != null) {
            result.put("achievements", achievements);
        }

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
