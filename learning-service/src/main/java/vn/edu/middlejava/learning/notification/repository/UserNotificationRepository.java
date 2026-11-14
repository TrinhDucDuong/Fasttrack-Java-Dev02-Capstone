package vn.edu.middlejava.learning.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.notification.entity.UserNotification;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
