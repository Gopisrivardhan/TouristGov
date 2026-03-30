package com.tourismgov.autosender;

import com.tourismgov.dto.NotificationRequestDTO;
import com.tourismgov.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

  
    @EventListener
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
}