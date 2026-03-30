package com.tourismgov.dto;

import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.enums.NotificationStatus;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long notificationId;


    private Long userId;
    private String userName;
    
    private String subject;

    // Related record ID
    private Long entityId;

    // Notification content
    private String message;
    private NotificationCategory category;
    private NotificationStatus status;       // READ or UNREAD
    private LocalDateTime createdDate;
}
