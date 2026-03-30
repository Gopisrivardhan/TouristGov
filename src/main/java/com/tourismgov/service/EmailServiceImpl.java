package com.tourismgov.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async 
    public void sendNotificationEmail(String to, String name, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            String htmlMsg = 
                "<div style='background-color: #f0f2f5; padding: 40px 10px; font-family: \"Segoe UI\", Helvetica, Arial, sans-serif;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.12);'>" +
                        
                        "<div style='background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); padding: 30px; text-align: center;'>" +
                            "<div style='color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: 1px; margin-bottom: 8px;'>TOURISM GOV INDIA</div>" +
                            "<div style='color: #a0c4ff; font-size: 14px; font-weight: 400; text-transform: uppercase; letter-spacing: 2px;'>Official Service Notification</div>" +
                        "</div>" +

                        "<div style='padding: 40px; color: #333333;'>" +
                            "<h2 style='color: #1e3c72; margin-top: 0; font-size: 20px;'>Important Account Update</h2>" +
                            "<p style='font-size: 16px; margin-bottom: 24px;'>Hello <strong>" + name + "</strong>,</p>" +
                            
                            "<div style='background-color: #f8fbff; padding: 20px; border-left: 5px solid #2a5298; border-radius: 4px; margin-bottom: 24px;'>" +
                                "<p style='font-size: 16px; line-height: 1.6; color: #444444; margin: 0;'>" + body + "</p>" +
                            "</div>" +

                            "<div style='text-align: center; margin-top: 35px;'>" +
                                "<a href='http://localhost:3000/notifications' style='background-color: #2a5298; color: #ffffff; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 15px; display: inline-block;'>View Details in Dashboard</a>" +
                            "</div>" +

                            "<p style='font-size: 13px; color: #888888; margin-top: 30px; line-height: 1.5;'>" +
                                "If you did not expect this notification, please secure your account or contact our 24/7 helpdesk immediately." +
                            "</p>" +
                        "</div>" +

                        "<div style='background-color: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #999999; border-top: 1px solid #eeeeee;'>" +
                            "© 2026 Ministry of Tourism | Digital India Portal<br>" +
                            "<div style='margin-top: 8px; color: #bbbbbb;'>This is an automated system-generated email. Please do not reply directly to this address.</div>" +
                        "</div>" +
                    "</div>" +
                "</div>";

            helper.setText(htmlMsg, true); 
            helper.setTo(to);
            helper.setSubject(subject);
            // Argument 1: The email. Argument 2: The Display Name.
            helper.setFrom("omkarchoramale05@gmail.com", "Tourism Gov Portal");

            mailSender.send(mimeMessage);
            log.info("Colorful HTML Email sent successfully to {}", to);
            
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding error for sender name: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            e.printStackTrace(); 
        }
    }
}