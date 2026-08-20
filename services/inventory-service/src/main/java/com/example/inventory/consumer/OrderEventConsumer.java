package com.example.inventory.consumer;

import com.example.inventory.event.OrderCreatedEvent;
import com.example.inventory.idempotency.ProcessedEvent;
import com.example.inventory.idempotency.ProcessedEventRepository;
import com.example.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private static final String CONSUMER_NAME = "inventory-service-order-consumer";

    @KafkaListener(topics = "orders.created", groupId = "inventory-service-group")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
            log.info("service=inventory-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received OrderCreatedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=inventory-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            inventoryService.processOrderCreated(event);

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge(); // Acknowledge only on success
        } catch (Exception e) {
            log.error("service=inventory-service topic={} partition={} offset={} message=\"Error processing event\"", record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e); // triggers retry/DLT
        }
    }
}
