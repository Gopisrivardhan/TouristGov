package com.tourismgov.controller;

import com.tourismgov.dto.NotificationRequestDTO;
import com.tourismgov.dto.NotificationResponseDTO;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tourismgov/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

   
    @PostMapping("/create")
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationRequestDTO request) {

        NotificationResponseDTO response = notificationService.create(request);


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

   
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAll(
            @RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.getAll(userId));
    }
    
    @GetMapping("/user/{userId}/{category}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByCategory(
            @PathVariable Long userId,
            @PathVariable NotificationCategory category) {
        
        List<NotificationResponseDTO> notifications = 
            notificationService.getByCategory(userId, category);
            
        return ResponseEntity.ok(notifications);
    }


   
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnread(
            @RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

   
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }
    
    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@Valid @RequestBody NotificationRequestDTO request) {
        
        // The Service handles: 1. User Validation, 2. Role Check, 3. Batch Saving
        notificationService.sendGlobalNotification(request);
        
        return ResponseEntity.ok("Global Broadcast Successful! Every user has been notified.");
    }
}
