package vn.edu.middlejava.learning.notification.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.learning.notification.dto.NotificationResponse;

import java.util.List;

public interface INotificationController {
    List<NotificationResponse> getNotifications(Jwt jwt);
}
