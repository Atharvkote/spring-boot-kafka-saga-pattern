package com.example.inventory.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class InventoryReleaseRequestedEvent extends BaseEvent {
    private String orderId;
    private String reason;

    @Builder
    public InventoryReleaseRequestedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId, String orderId, String reason) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.reason = reason;
    }
}
