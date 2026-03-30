package com.tourismgov.service;

import com.tourismgov.dto.NotificationRequestDTO;
import com.tourismgov.dto.NotificationResponseDTO;
import com.tourismgov.model.Notification;
import com.tourismgov.model.User;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.enums.NotificationStatus;
import com.tourismgov.enums.Role;
import com.tourismgov.exception.ResourceNotFoundException;
import com.tourismgov.repository.NotificationRepository;
import com.tourismgov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.tourismgov.exception.ErrorMessages.UNAUTHORIZED_ACTION;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService; 

    @Override
    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .entityId(request.getEntityId())
                .subject(request.getSubject())
                .message(request.getMessage())
                .category(request.getCategory())
                .status(NotificationStatus.UNREAD)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        sendEmailSafely(user, request.getSubject(), request.getMessage());

        return toDTO(savedNotification);
    }

    @Override
    @Transactional
    public void sendGlobalNotification(NotificationRequestDTO request) {
        User sender = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (sender.getRole().equals(Role.TOURIST.name())) {
            // THE FIX: The RuntimeException is now fully replaced!
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, UNAUTHORIZED_ACTION);
        }

        List<User> allUsers = userRepository.findAll();

        List<Notification> batch = allUsers.stream()
            .map(user -> Notification.builder()
                .user(user)
                .subject(request.getSubject())
                .message(request.getMessage())
                .category(request.getCategory())
                .entityId(request.getEntityId() != null ? request.getEntityId() : 0L)
                .status(NotificationStatus.UNREAD)
                .build())
            .toList();

        notificationRepository.saveAll(batch);

        allUsers.forEach(user -> sendEmailSafely(user, request.getSubject(), request.getMessage()));
    }

    private void sendEmailSafely(User user, String subject, String message) {
        try {
            emailService.sendNotificationEmail(user.getEmail(), user.getName(), subject, message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAll(Long userId) {
        verifyUserExists(userId);
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId) 
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnread(Long userId) {
        verifyUserExists(userId);
        return notificationRepository
                .findByUser_UserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.UNREAD)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUser_UserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        notification.setStatus(NotificationStatus.READ);
        return toDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category) {
        verifyUserExists(userId);
        return notificationRepository
                .findByUser_UserIdAndCategoryOrderByCreatedAtDesc(userId, category)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void verifyUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
    }

    private NotificationResponseDTO toDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .userName(notification.getUser().getName())
                .entityId(notification.getEntityId())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .category(notification.getCategory())
                .status(notification.getStatus())
                .createdDate(notification.getCreatedAt())
                .build();
    }
}