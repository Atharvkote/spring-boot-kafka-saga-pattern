package com.example.orders.service;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.entity.Order;
import com.example.orders.entity.Order.OrderStatus;
import com.example.orders.event.model.*;
import com.example.orders.event.producer.EventProducer;
import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStateService orderStateService;
    private final EventProducer eventProducer;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create OrderCreatedEvent
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .occurredAt(LocalDateTime.now())
                .correlationId("CORR-" + savedOrder.getCustomerId() + "-" + orderId)
                .orderId(orderId.toString())
                .customerId(savedOrder.getCustomerId())
                .totalAmount(savedOrder.getTotalAmount())
                .currency(savedOrder.getCurrency())
                .build();

        // Publish to orders.created topic
        eventProducer.publish("orders.created", orderId.toString(), event);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrder(UUID id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<OrderResponse> listOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderStateService.validateTransition(order.getStatus(), OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled successfully. orderId={}", id);
        return mapToResponse(savedOrder);
    }

    @Transactional
    public void handleInventoryReserved(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found during inventory reservation. orderId={}", orderId);
            return;
        }

        // Validate and transition to INVENTORY_RESERVED
        orderStateService.validateTransition(order.getStatus(), OrderStatus.INVENTORY_RESERVED);
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        // Then transition to PAYMENT_PENDING
        orderStateService.validateTransition(order.getStatus(), OrderStatus.PAYMENT_PENDING);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);
        log.info("Order status updated to PAYMENT_PENDING. orderId={}", orderId);
    }

    @Transactional
    public void handleInventoryFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found during inventory failure. orderId={}", orderId);
            return;
        }

        orderStateService.validateTransition(order.getStatus(), OrderStatus.INVENTORY_FAILED);
        order.setStatus(OrderStatus.INVENTORY_FAILED);
        orderRepository.save(order);
        log.info("Order status updated to INVENTORY_FAILED. orderId={}", orderId);
    }

    @Transactional
    public void handlePaymentCompleted(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found during payment completed. orderId={}", orderId);
            return;
        }

        orderStateService.validateTransition(order.getStatus(), OrderStatus.PAID);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order status updated to PAID. orderId={}", orderId);
    }

    @Transactional
    public void handlePaymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found during payment failure. orderId={}", orderId);
            return;
        }

        orderStateService.validateTransition(order.getStatus(), OrderStatus.PAYMENT_FAILED);
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        log.info("Order status updated to PAYMENT_FAILED. orderId={}", orderId);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
