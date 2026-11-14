package vn.edu.middlejava.learning.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.notification.dto.NotificationResponse;
import vn.edu.middlejava.learning.notification.service.INotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationControllerImpl implements INotificationController {
    private final INotificationService notifications;

    public NotificationControllerImpl(INotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    @GetMapping
    public List<NotificationResponse> getNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notifications.getForUser(Long.valueOf(jwt.getSubject()));
    }
}
