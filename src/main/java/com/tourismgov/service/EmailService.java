package com.tourismgov.service;



public interface EmailService {
    /**
     * Sends a simple text email asynchronously.
     * @param to Recipient email address
     * @param subject Email subject line
     * @param body Email content text
     */
    void sendNotificationEmail(String to,String name, String subject, String body);
}
