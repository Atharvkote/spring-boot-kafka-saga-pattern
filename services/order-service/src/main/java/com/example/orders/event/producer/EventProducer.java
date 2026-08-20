package com.example.orders.event.producer;

import com.example.orders.event.model.BaseEvent;

public interface EventProducer {
    void publish(String topic, String key, BaseEvent event);
}
