package com.example.orders.event.consumer;

import com.example.orders.event.model.PaymentCompletedEvent;
import com.example.orders.event.model.PaymentFailedEvent;
import com.example.orders.idempotency.ProcessedEvent;
import com.example.orders.idempotency.ProcessedEventRepository;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private static final String CONSUMER_NAME = "payment-event-consumer";

    @KafkaListener(topics = "payment.completed", groupId = "order-service-group")
    @Transactional
    public void consumeCompleted(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(record.value(), PaymentCompletedEvent.class);
            log.info("service=order-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received PaymentCompletedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=order-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            orderService.handlePaymentCompleted(UUID.fromString(event.getOrderId()));

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("service=order-service topic={} partition={} offset={} message=\"Error processing payment completed event\"",
                    record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service-group")
    @Transactional
    public void consumeFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(record.value(), PaymentFailedEvent.class);
            log.info("service=order-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received PaymentFailedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=order-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            orderService.handlePaymentFailed(UUID.fromString(event.getOrderId()));

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("service=order-service topic={} partition={} offset={} message=\"Error processing payment failed event\"",
                    record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }
}
