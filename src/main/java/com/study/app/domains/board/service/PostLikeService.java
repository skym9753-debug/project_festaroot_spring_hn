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

    @Autowired
    private com.study.app.domains.activity.UserActivityLogService userActivityLogService;

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

            // 1. 좋아요 누른 사람 보상 (최초 1회만 지급)
            if (!userActivityLogService.isAlreadyRewarded(member_id, "LIKE_GIVEN", post_id)) {
                achievements = achievementService.addActivityExp(member_id, com.study.app.domains.achievement.AchievementService.ActivityType.LIKE_GIVEN);
                
                // 활동 로그 기록 (취소해도 남겨서 중복 지급 방지)
                com.study.app.domains.activity.dto.UserActivityLogDTO log = new com.study.app.domains.activity.dto.UserActivityLogDTO();
                log.setMember_id(member_id);
                log.setAction_type("LIKE_GIVEN");
                log.setContent_id(post_id);
                userActivityLogService.saveLog(log);
            }

            // 2. 좋아요 받은 사람 보상 및 알림
            com.study.app.domains.board.dto.CommunityPostDTO post = postDAO.selectById(post_id);
            if (post != null && !member_id.equals(post.getMember_id())) {
                // 업적 연동 (이 게시글에 대해 이 유저가 처음 좋아요를 눌렀을 때만 작성자에게 점수 부여)
                String actionKey = "RECEIVE_LIKE_FROM_" + member_id; // 작성자 입장에서 '누구에게 받았나'로 구분하거나, 
                                                                   // 단순히 중복 방지를 위해 로그 활용
                
                // 더 정확한 어뷰징 방지: "이 글에 대해 이 유저가 보상을 준 적이 있는가"를 별도 로그로 관리하거나
                // 여기서는 단순히 'RECEIVE_LIKE' 타입을 체크하되, content_id(글번호)와 member_id(누른사람) 조합을 고민해야 함.
                // 간단하게 "이 글에 대해 이 사람이 처음 누른 거라면"으로 처리하기 위해 
                // RECEIVE_LIKE 로그의 content_id를 활용하되, member_id는 글주인, 
                // 키워드(keyword) 필드에 누른 사람의 ID를 저장하는 방식을 제안합니다.
                
                if (!userActivityLogService.isAlreadyRewarded(post.getMember_id(), "RECEIVE_LIKE_DETAIL", post_id)) {
                    // 실제로는 "이 글에 좋아요 점수를 이미 받았는지" 여부를 체크하는 것이 안전함 (글당 1번만 점수)
                    // 만약 좋아요를 받을 때마다 점수를 주고 싶다면 이 로직은 빼야 하지만, 
                    // 무한 반복을 막으려면 "이 글에서 얻을 수 있는 최대 좋아요 점수" 등을 제한해야 함.
                    // 여기서는 "사용자당 글 하나에 대해 1번만 점수"를 주는 방식으로 구현합니다.
                    
                    achievementService.addActivityExp(post.getMember_id(), com.study.app.domains.achievement.AchievementService.ActivityType.RECEIVE_LIKE);
                    
                    com.study.app.domains.activity.dto.UserActivityLogDTO receiveLog = new com.study.app.domains.activity.dto.UserActivityLogDTO();
                    receiveLog.setMember_id(post.getMember_id());
                    receiveLog.setAction_type("RECEIVE_LIKE_DETAIL");
                    receiveLog.setContent_id(post_id);
                    userActivityLogService.saveLog(receiveLog);
                }

                // 실시간 좋아요 알림 생성 (알림은 누를 때마다 보낼 수도 있으나 보통 이것도 로그 체크 가능)
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
