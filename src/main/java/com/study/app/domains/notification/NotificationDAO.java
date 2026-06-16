package com.study.app.domains.notification;

import java.util.List;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.study.app.domains.notification.dto.NotificationDTO;

@Repository
public class NotificationDAO {

    @Autowired
    private SqlSessionTemplate mybatis;

    public int insertNotification(NotificationDTO dto) {
        return mybatis.insert("Notification.insertNotification", dto);
    }

    public List<NotificationDTO> selectUnreadNotifications(String memberId) {
        return mybatis.selectList("Notification.selectUnreadNotifications", memberId);
    }

    public int updateToRead(Long notiId) {
        return mybatis.update("Notification.updateToRead", notiId);
    }
}
