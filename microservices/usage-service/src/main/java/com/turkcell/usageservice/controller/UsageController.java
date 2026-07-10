package com.turkcell.usageservice.controller;

import com.turkcell.usageservice.model.entity.Quota;
import com.turkcell.usageservice.model.entity.UsageRecord;
import com.turkcell.usageservice.service.UsageQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

    private final UsageQueryService queryService;

    public UsageController(UsageQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/subscriptions/{id}/quota")
    public ResponseEntity<Quota> getActiveQuota(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.getActiveQuota(id));
    }

    @GetMapping("/subscriptions/{id}/history")
    public ResponseEntity<List<UsageRecord>> getUsageHistory(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(queryService.getUsageHistory(id, from, to));
    }
}
