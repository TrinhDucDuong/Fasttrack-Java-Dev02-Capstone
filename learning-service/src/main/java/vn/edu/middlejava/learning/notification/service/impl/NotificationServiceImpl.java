package vn.edu.middlejava.learning.notification.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.notification.dto.NotificationResponse;
import vn.edu.middlejava.learning.notification.repository.UserNotificationRepository;
import vn.edu.middlejava.learning.notification.service.INotificationService;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {
    private final UserNotificationRepository notifications;

    public NotificationServiceImpl(UserNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notification -> new NotificationResponse(notification.getId(), notification.getOrderId(),
                        notification.getMessage(), notification.getCreatedAt(), notification.getReadAt() != null))
                .toList();
    }
}
