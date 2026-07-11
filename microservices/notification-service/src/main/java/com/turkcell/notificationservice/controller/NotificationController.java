package com.turkcell.notificationservice.controller;

import com.turkcell.notificationservice.dto.NotificationRequest;
import com.turkcell.notificationservice.model.entity.Notification;
import com.turkcell.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.processEventAndNotify(request.getTemplateCode(), request.getUserId(), request.getPayloadJson());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/users/{id}/history")
    public ResponseEntity<List<Notification>> getUserHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getUserHistory(id));
    }
}
