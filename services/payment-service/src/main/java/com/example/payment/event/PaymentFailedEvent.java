package com.example.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class PaymentFailedEvent extends BaseEvent {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String reason;

    @Builder
    public PaymentFailedEvent(String eventId, String eventType, LocalDateTime occurredAt, String correlationId,
                              String orderId, BigDecimal amount, String currency, String reason) {
        super(eventId, eventType, occurredAt, correlationId);
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
    }
}
