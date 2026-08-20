package com.example.notification.service;

import com.example.notification.notification.ConsoleNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ConsoleNotificationSender notificationSender;

    public void sendNotification(String topic, Map<String, Object> event) {
        log.info("Triggering notification send logic for topic={}", topic);
        notificationSender.send(topic, event);
    }
}
