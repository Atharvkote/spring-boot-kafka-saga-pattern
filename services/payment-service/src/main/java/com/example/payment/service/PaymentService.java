package com.example.payment.service;

import com.example.payment.event.InventoryReservedEvent;
import com.example.payment.event.PaymentCompletedEvent;
import com.example.payment.event.PaymentFailedEvent;
import com.example.payment.processor.PaymentProcessor;
import com.example.payment.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentProcessor paymentProcessor;
    private final PaymentEventProducer eventProducer;

    public void processInventoryReserved(InventoryReservedEvent event) {
        log.info("Processing payment for orderId={} amount={} {}", event.getOrderId(), event.getAmount(), event.getCurrency());

        // Extract customerId from correlationId or set default.
        String customerId = event.getCorrelationId();

        boolean processed = paymentProcessor.process(event.getOrderId(), event.getAmount(), event.getCurrency(), customerId);

        if (processed) {
            PaymentCompletedEvent successEvent = PaymentCompletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PaymentCompleted")
                    .occurredAt(LocalDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .orderId(event.getOrderId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .paymentId(UUID.randomUUID().toString())
                    .build();

            eventProducer.publish("payment.completed", event.getOrderId(), successEvent);
            log.info("Payment completed event published for orderId={}", event.getOrderId());
        } else {
            PaymentFailedEvent failEvent = PaymentFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PaymentFailed")
                    .occurredAt(LocalDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .orderId(event.getOrderId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .reason("Insufficient funds or card declined")
                    .build();

            eventProducer.publish("payment.failed", event.getOrderId(), failEvent);
            log.warn("Payment failed event published for orderId={}", event.getOrderId());
        }
    }
}
