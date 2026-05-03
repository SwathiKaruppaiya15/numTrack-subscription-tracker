package com.subtrack.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UsageLogResponse {
    private UUID id;
    private UUID subscriptionId;
    private String subscriptionName;
    private LocalDateTime usedAt;
    private String notes;
}
