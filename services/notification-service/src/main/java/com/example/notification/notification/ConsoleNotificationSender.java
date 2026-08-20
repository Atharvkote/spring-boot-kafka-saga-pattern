package com.example.notification.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ConsoleNotificationSender {

    public void send(String topic, Map<String, Object> event) {
        String orderId = (String) event.get("orderId");
        String correlationId = (String) event.get("correlationId");

        switch (topic) {
            case "orders.created":
                String customerId = (String) event.get("customerId");
                Object amount = event.get("totalAmount");
                Object currency = event.get("currency");
                log.info(">>>> [NOTIFICATION SENT] Customer {} notified: Order {} created for amount {} {}. CorrelationId: {}",
                        customerId, orderId, amount, currency, correlationId);
                break;
            case "payment.completed":
                log.info(">>>> [NOTIFICATION SENT] Customer notified: Payment completed successfully for Order {}. CorrelationId: {}",
                        orderId, correlationId);
                break;
            case "payment.failed":
                Object reason = event.getOrDefault("reason", "Unknown payment error");
                log.warn(">>>> [NOTIFICATION SENT] Customer notified: Payment failed for Order {}. Reason: {}. CorrelationId: {}",
                        orderId, reason, correlationId);
                break;
            default:
                log.info(">>>> [NOTIFICATION SENT] Event received on topic {}: {}. CorrelationId: {}",
                        topic, event, correlationId);
                break;
        }
    }
}
