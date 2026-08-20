package com.example.payment.consumer;

import com.example.payment.event.InventoryReservedEvent;
import com.example.payment.idempotency.ProcessedEvent;
import com.example.payment.idempotency.ProcessedEventRepository;
import com.example.payment.service.PaymentService;
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
public class InventoryEventConsumer {

    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private static final String CONSUMER_NAME = "payment-service-inventory-consumer";

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-service-group")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(record.value(), InventoryReservedEvent.class);
            log.info("service=payment-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Received InventoryReservedEvent\"",
                    event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId(), record.topic(), record.partition(), record.offset());

            if (processedEventRepository.existsByEventIdAndConsumerName(event.getEventId(), CONSUMER_NAME)) {
                log.warn("service=payment-service eventType={} eventId={} correlationId={} orderId={} message=\"Duplicate event detected, skipping process\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), event.getOrderId());
                ack.acknowledge();
                return;
            }

            paymentService.processInventoryReserved(event);

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .consumerName(CONSUMER_NAME)
                    .build());

            ack.acknowledge(); // Acknowledge only on success
        } catch (Exception e) {
            log.error("service=payment-service topic={} partition={} offset={} message=\"Error processing event\"", record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException("Failed to process event", e); // triggers retry/DLT
        }
    }
}
