package com.example.orders.event.consumer;

import com.example.orders.event.model.InventoryFailedEvent;
import com.example.orders.event.model.InventoryReservedEvent;
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
public class InventoryEventConsumer {

    private final OrderService orderService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private static final String CONSUMER_NAME = "inventory-event-consumer";

    @KafkaListener(topics = "inventory.reserved", groupId = "order-service-group")
    @Transactional
    public void consumeReserved(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(record.value(), InventoryReservedEvent.class);
            log.info("service=order-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received InventoryReservedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=order-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            orderService.handleInventoryReserved(UUID.fromString(event.getOrderId()));

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("service=order-service topic={} partition={} offset={} message=\"Error processing inventory reserved event\"",
                    record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-service-group")
    @Transactional
    public void consumeFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            InventoryFailedEvent event = objectMapper.readValue(record.value(), InventoryFailedEvent.class);
            log.info("service=order-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received InventoryFailedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=order-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            orderService.handleInventoryFailed(UUID.fromString(event.getOrderId()));

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("service=order-service topic={} partition={} offset={} message=\"Error processing inventory failed event\"",
                    record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }
}
