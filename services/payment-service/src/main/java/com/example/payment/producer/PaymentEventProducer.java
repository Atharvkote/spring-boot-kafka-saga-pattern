package com.example.payment.producer;

import com.example.payment.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, BaseEvent event) {
        log.info("service=payment-service eventType={} eventId={} correlationId={} orderId={} topic={} message=\"Publishing event\"",
                event.getEventType(), event.getEventId(), event.getCorrelationId(), key, topic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("service=payment-service eventType={} eventId={} correlationId={} orderId={} topic={} partition={} offset={} message=\"Successfully published event\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), key, topic,
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error("service=payment-service eventType={} eventId={} correlationId={} orderId={} topic={} message=\"Failed to publish event\"",
                        event.getEventType(), event.getEventId(), event.getCorrelationId(), key, topic, ex);
            }
        });
    }
}
