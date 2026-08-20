package com.example.orders.service;

import com.example.orders.entity.Order;
import com.example.orders.entity.Order.OrderStatus;
import com.example.orders.exception.InvalidOrderStateException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class OrderStateService {

    private final Map<OrderStatus, Set<OrderStatus>> validTransitions = new HashMap<>();

    public OrderStateService() {
        validTransitions.put(OrderStatus.PENDING, EnumSet.of(
                OrderStatus.INVENTORY_RESERVED,
                OrderStatus.INVENTORY_FAILED,
                OrderStatus.CANCELLED
        ));
        validTransitions.put(OrderStatus.INVENTORY_RESERVED, EnumSet.of(
                OrderStatus.PAYMENT_PENDING,
                OrderStatus.CANCELLED
        ));
        validTransitions.put(OrderStatus.PAYMENT_PENDING, EnumSet.of(
                OrderStatus.PAID,
                OrderStatus.PAYMENT_FAILED,
                OrderStatus.CANCELLED
        ));
        // Terminal states
        validTransitions.put(OrderStatus.PAID, EnumSet.noneOf(OrderStatus.class));
        validTransitions.put(OrderStatus.INVENTORY_FAILED, EnumSet.noneOf(OrderStatus.class));
        validTransitions.put(OrderStatus.PAYMENT_FAILED, EnumSet.noneOf(OrderStatus.class));
        validTransitions.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public void validateTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        Set<OrderStatus> allowed = validTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidOrderStateException(currentStatus, targetStatus);
        }
    }
}
