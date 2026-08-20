package com.example.inventory.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class InventoryReleasedEvent extends BaseEvent {
    private String orderId;

    @Builder
    public InventoryReleasedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId, String orderId) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
    }
}
