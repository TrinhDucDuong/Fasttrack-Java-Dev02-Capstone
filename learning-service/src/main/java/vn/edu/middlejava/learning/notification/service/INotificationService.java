package vn.edu.middlejava.learning.notification.service;

import vn.edu.middlejava.learning.notification.dto.NotificationResponse;

import java.util.List;

public interface INotificationService {
    List<NotificationResponse> getForUser(Long userId);
}
