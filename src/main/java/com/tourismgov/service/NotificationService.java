package com.tourismgov.service;

import com.tourismgov.dto.NotificationRequestDTO;
import com.tourismgov.dto.NotificationResponseDTO;
import com.tourismgov.enums.NotificationCategory;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO create(NotificationRequestDTO request);

    List<NotificationResponseDTO> getAll(Long userId);

    List<NotificationResponseDTO> getUnread(Long userId);

    NotificationResponseDTO markAsRead(Long notificationId, Long userId);
    
    List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category);
    
    void sendGlobalNotification(NotificationRequestDTO request);
}