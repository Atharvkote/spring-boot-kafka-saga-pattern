package com.example.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class InventoryReservedEvent extends BaseEvent {
    private String orderId;
    private BigDecimal amount;
    private String currency;

    @Builder
    public InventoryReservedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId,
                                  String orderId, BigDecimal amount, String currency) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }
}
