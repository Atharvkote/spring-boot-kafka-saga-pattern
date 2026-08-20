package com.example.inventory.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String currency;

    @Builder
    public OrderCreatedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId,
                             String orderId, String customerId, BigDecimal totalAmount, String currency) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.currency = currency;
    }
}
