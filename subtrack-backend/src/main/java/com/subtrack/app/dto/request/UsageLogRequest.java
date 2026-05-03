package com.subtrack.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UsageLogRequest {

    @NotNull(message = "Subscription ID is required")
    private UUID subscriptionId;

    private String notes;
}
