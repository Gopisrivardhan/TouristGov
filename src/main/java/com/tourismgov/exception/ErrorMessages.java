package com.tourismgov.exception;

public final class ErrorMessages {
    
    private ErrorMessages() {} // Prevent instantiation

    // General Entities
    public static final String EVENT_NOT_FOUND = "Event not found";
    public static final String SITE_NOT_FOUND = "Heritage site not found";
    public static final String PROGRAM_NOT_FOUND = "Tourism program not found";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String RECORD_NOT_FOUND = "Record not found";

    // Tourist Specific
    public static final String ERROR_TOURIST_NOT_FOUND = "Tourist not found with ID: %d";
    public static final String ERROR_DUPLICATE_CONTACT_INFO = "This email address is already registered: %s";
    public static final String ERROR_UNDERAGE_TOURIST = "Tourist must be at least 18 years old";
    
    // Document Specific
    public static final String ERROR_DOCUMENT_NOT_FOUND = "Document not found with ID: %d for Tourist ID: %d";
    public static final String ERROR_INVALID_DOCUMENT_TYPE = "Invalid document type: %s";
    public static final String ERROR_MISSING_DOCUMENT_INPUT = "Missing file or fileUri";
    public static final String ERROR_INVALID_VERIFICATION_STATUS = "Invalid verification status: %s";
    public static final String ERROR_FILE_SAVE = "Could not save file: %s";
    public static final String ERROR_FILE_DELETE = "Failed to delete physical file: %s";

    // Security
    public static final String INVALID_OLD_PASSWORD = "Old password is incorrect";
    public static final String UNAUTHORIZED_ACTION = "You do not have permission to do this";
    
    
    // Business Logic Errors
    public static final String DUPLICATE_BOOKING = "You have already confirmed a booking for this event.";
    public static final String INVALID_DATE_RANGE = "The program end date cannot be earlier than the start date.";
}