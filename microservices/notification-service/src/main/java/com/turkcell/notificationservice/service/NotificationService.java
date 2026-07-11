package com.turkcell.notificationservice.service;

import com.turkcell.notificationservice.model.entity.Notification;
import com.turkcell.notificationservice.model.entity.NotificationTemplate;
import com.turkcell.notificationservice.repository.NotificationRepository;
import com.turkcell.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;

    public void processEventAndNotify(String templateCode, UUID userId, String payloadJson) {
        log.info("Processing notification for template: {}, user: {}", templateCode, userId);
        
        Optional<NotificationTemplate> templateOpt = templateRepository.findByCodeAndChannel(templateCode, "EMAIL");
        
        if (templateOpt.isPresent()) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .templateCode(templateCode)
                    .channel("EMAIL")
                    .payloadJson(payloadJson)
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            log.info("Notification sent successfully to user: {}", userId);
        } else {
            log.warn("No active template found for code: {}", templateCode);
        }
    }

    public List<Notification> getUserHistory(UUID userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }
}
