package com.tourismgov.autosender;

import com.tourismgov.dto.NotificationRequestDTO;
import com.tourismgov.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    // 1. Handles Targeted Notifications (e.g., "Your booking is approved")
    @EventListener
    @Async // Good practice: free up the main thread immediately
    public void handleAutomaticNotification(ActivityEvent event) {
        NotificationRequestDTO dto = NotificationRequestDTO.builder()
                .userId(event.getUserId())
                .entityId(event.getEntityId())
                .subject(event.getSubject())
                .message(event.getMessage())
                .category(event.getCategory())
                .build();

        notificationService.create(dto);
    }

    // 2. Handles Global Notifications (e.g., "New Event Created!")
    @EventListener
    @Async
    public void handleGlobalNotification(GlobalActivityEvent event) {
        NotificationRequestDTO dto = NotificationRequestDTO.builder()
                .userId(event.getSenderId()) // Used to verify they aren't a TOURIST
                .entityId(event.getEntityId())
                .subject(event.getSubject())
                .message(event.getMessage())
                .category(event.getCategory())
                .build();

        notificationService.sendGlobalNotification(dto);
    }
}