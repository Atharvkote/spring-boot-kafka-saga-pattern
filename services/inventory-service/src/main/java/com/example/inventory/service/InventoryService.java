package com.example.inventory.service;

import com.example.inventory.event.InventoryFailedEvent;
import com.example.inventory.event.InventoryReservedEvent;
import com.example.inventory.event.OrderCreatedEvent;
import com.example.inventory.processor.InventoryProcessor;
import com.example.inventory.producer.InventoryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryProcessor inventoryProcessor;
    private final InventoryEventProducer eventProducer;

    public void processOrderCreated(OrderCreatedEvent event) {
        log.info("Processing inventory reservation for orderId={} customerId={}", event.getOrderId(), event.getCustomerId());

        boolean reserved = inventoryProcessor.checkAndReserve(event.getOrderId(), event.getCustomerId());

        if (reserved) {
            InventoryReservedEvent successEvent = InventoryReservedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("InventoryReserved")
                    .occurredAt(LocalDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .orderId(event.getOrderId())
                    .amount(event.getTotalAmount())
                    .currency(event.getCurrency())
                    .build();

            eventProducer.publish("inventory.reserved", event.getOrderId(), successEvent);
            log.info("Inventory reserved event published for orderId={}", event.getOrderId());
        } else {
            InventoryFailedEvent failEvent = InventoryFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("InventoryFailed")
                    .occurredAt(LocalDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .orderId(event.getOrderId())
                    .reason("Insufficient inventory stock")
                    .build();

            eventProducer.publish("inventory.failed", event.getOrderId(), failEvent);
            log.warn("Inventory failed event published for orderId={}", event.getOrderId());
        }
    }
}
