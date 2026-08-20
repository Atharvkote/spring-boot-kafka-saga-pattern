package com.example.payment.processor;

import java.math.BigDecimal;

public interface PaymentProcessor {
    boolean process(String orderId, BigDecimal amount, String currency, String customerId);
}
