package com.study.app.domains.notification;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.notification.dto.NotificationDTO;
import com.study.app.utils.JWTUtil;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JWTUtil jwt;

    /**
     * 로그인한 사용자의 읽지 않은 알림 목록을 조회합니다.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String memberId = jwt.getSubject(token);
        
        List<NotificationDTO> list = notificationService.getUnreadNotifications(memberId);
        return ResponseEntity.ok(list);
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     */
    @PutMapping("/{notiId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notiId) {
        notificationService.markAsRead(notiId);
        return ResponseEntity.ok().build();
    }
}
