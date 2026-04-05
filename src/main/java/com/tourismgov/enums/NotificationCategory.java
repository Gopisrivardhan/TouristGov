package com.tourismgov.enums;

/**
 * NotificationCategory enum - type/category of notification.
 *
 * As per document Section 4.8 (Notifications and Alerts) entity:
 * Notification(NotificationID, UserID, EntityID, Message,
 * Category [Site/Event/Program/Compliance], Status, CreatedDate)
 */
public enum NotificationCategory {
    SITE,         // Related to heritage sites and monuments
    EVENT,        // Related to events and tour bookings
    PROGRAM,      // Related to tourism programs and resources
    COMPLIANCE,   // Related to compliance records and audits
    SYSTEM ,       // Used for global broadcasts and system-wide alerts
    BOOKING,ALERT
}