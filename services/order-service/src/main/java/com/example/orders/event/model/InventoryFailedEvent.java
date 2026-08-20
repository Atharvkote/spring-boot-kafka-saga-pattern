package com.example.orders.event.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class InventoryFailedEvent extends BaseEvent {
    private String orderId;
    private String reason;

    @Builder
    public InventoryFailedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId, String orderId, String reason) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.reason = reason;
    }
}
