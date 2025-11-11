package com.hka.ps.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PsPublisher {
    private final RabbitTemplate rabbitTemplate;

    public PsPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message) {
        String logEntry = "[PS] " + message;
        rabbitTemplate.convertAndSend("log.queue", logEntry);
    }
}
