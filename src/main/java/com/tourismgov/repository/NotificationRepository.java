package com.tourismgov.repository;

import com.tourismgov.model.Notification;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // These are the methods that Spring Data JPA uses to search the database!
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_UserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);

    List<Notification> findByUser_UserIdAndCategoryOrderByCreatedAtDesc(Long userId, NotificationCategory category);

    Optional<Notification> findByNotificationIdAndUser_UserId(Long notificationId, Long userId);
}