package com.example.inventory.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class InventoryProcessor {

    private final double failureRate;
    private final Random random = new Random();

    public InventoryProcessor(@Value("${inventory.failure-rate:0.10}") double failureRate) {
        this.failureRate = failureRate;
        log.info("Initialized InventoryProcessor with simulated failure-rate = {}", failureRate);
    }

    public boolean checkAndReserve(String orderId, String customerId) {
        // Deterministic test check: if customerId starts with or is "fail-inventory", always fail!
        if (customerId != null && customerId.toLowerCase().contains("fail-inventory")) {
            log.info("Deterministic test: forcing inventory failure for orderId={} customerId={}", orderId, customerId);
            return false;
        }

        double val = random.nextDouble();
        boolean success = val >= failureRate;
        log.info("Simulated inventory check for orderId={}: random={}, failureRate={}, outcome={}",
                orderId, val, failureRate, success ? "SUCCESS" : "FAILURE");
        return success;
    }
}
