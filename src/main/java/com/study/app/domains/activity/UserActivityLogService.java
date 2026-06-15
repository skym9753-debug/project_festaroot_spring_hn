package com.study.app.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.study.app.domains.activity.dto.UserActivityLogDTO;

@Service
public class UserActivityLogService {

    @Autowired
    private UserActivityLogDAO userActivityLogDAO;

    public int saveLog(UserActivityLogDTO log) {
        return userActivityLogDAO.insertLog(log);
    }

    public List<UserActivityLogDTO> getRecentLogs(String memberId) {
        return userActivityLogDAO.selectRecentLogs(memberId);
    }

    public boolean isAlreadyRewarded(String memberId, String actionType, Long contentId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("member_id", memberId);
        params.put("action_type", actionType);
        params.put("content_id", contentId);
        return userActivityLogDAO.existsLog(params) > 0;
    }
}
