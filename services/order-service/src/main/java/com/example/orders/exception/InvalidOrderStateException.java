package com.example.orders.exception;

import com.example.orders.entity.Order.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Invalid order state transition from " + currentStatus + " to " + targetStatus);
    }
}
