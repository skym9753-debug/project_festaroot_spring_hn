package com.study.app.domains.notification;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.study.app.domains.notification.dto.NotificationDTO;

@Service
public class NotificationService {

    @Autowired
    private NotificationDAO notificationDAO;

    public int saveNotification(NotificationDTO dto) {
        return notificationDAO.insertNotification(dto);
    }

    public List<NotificationDTO> getUnreadNotifications(String memberId) {
        return notificationDAO.selectUnreadNotifications(memberId);
    }

    public int markAsRead(Long notiId) {
        return notificationDAO.updateToRead(notiId);
    }
}
