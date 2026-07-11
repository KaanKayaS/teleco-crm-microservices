package com.turkcell.notificationservice.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequest {
    private UUID userId;
    private String templateCode;
    private String payloadJson;
}
