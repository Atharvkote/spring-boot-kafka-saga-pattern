package com.example.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final com.example.notification.service.NotificationService notificationService;

    @SuppressWarnings("unchecked")
    @KafkaListener(topics = {"orders.created", "payment.completed", "payment.failed"}, groupId = "notification-service-group")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            Map<String, Object> event = objectMapper.readValue(record.value(), Map.class);
            String eventType = (String) event.getOrDefault("eventType", "UnknownEvent");
            String orderId = (String) event.get("orderId");
            String correlationId = (String) event.get("correlationId");

            log.info("service=notification-service eventType={} orderId={} correlationId={} topic={} partition={} offset={} message=\"Received event for notification\"",
                    eventType, orderId, correlationId, record.topic(), record.partition(), record.offset());

            notificationService.sendNotification(record.topic(), event);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("service=notification-service topic={} partition={} offset={} message=\"Error processing notification event\"",
                    record.topic(), record.partition(), record.offset(), e);
            // We acknowledge to avoid blocking consumer partition for simulated logger service
            ack.acknowledge();
        }
    }
}
