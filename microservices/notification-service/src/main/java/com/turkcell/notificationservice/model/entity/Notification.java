package com.turkcell.notificationservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private UUID id;
    private UUID userId;
    private String templateCode;
    private String channel;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String payloadJson;
    private String status;
    private LocalDateTime sentAt;
}
