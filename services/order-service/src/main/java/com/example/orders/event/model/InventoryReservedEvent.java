package com.example.orders.event.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class InventoryReservedEvent extends BaseEvent {
    private String orderId;
    private java.math.BigDecimal amount;
    private String currency;

    @Builder
    public InventoryReservedEvent(String eventId, String eventType, java.time.LocalDateTime occurredAt, String correlationId,
                                  String orderId, java.math.BigDecimal amount, String currency) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }
}
