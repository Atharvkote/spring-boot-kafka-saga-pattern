package com.example.payment.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
@Slf4j
public class DemoPaymentProcessor implements PaymentProcessor {

    private final double failureRate;
    private final Random random = new Random();

    public DemoPaymentProcessor(@Value("${payment.failure-rate:0.10}") double failureRate) {
        this.failureRate = failureRate;
        log.info("Initialized DemoPaymentProcessor with simulated failure-rate = {}", failureRate);
    }

    @Override
    public boolean process(String orderId, BigDecimal amount, String currency, String customerId) {
        // Deterministic test check: if customerId contains "fail-payment", always fail!
        if (customerId != null && customerId.toLowerCase().contains("fail-payment")) {
            log.info("Deterministic test: forcing payment failure for orderId={} customerId={}", orderId, customerId);
            return false;
        }

        double val = random.nextDouble();
        boolean success = val >= failureRate;
        log.info("Simulated payment of {} {} for orderId={}: random={}, failureRate={}, outcome={}",
                amount, currency, orderId, val, failureRate, success ? "SUCCESS" : "FAILURE");
        return success;
    }
}
