package com.tourismgov.autosender;

import com.tourismgov.enums.NotificationCategory;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * LOGIC: This carries the data needed for a notification.
 * It is NOT a database entity.
 */
@Getter

@AllArgsConstructor
public class ActivityEvent {
    private final Long userId;
    private final String userName; // <--- ADD THIS
    private final Long entityId;
    private final String subject;
    private final String message;
    private final NotificationCategory category;
}