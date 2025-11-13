package com.hka.ps.publisher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PsPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PsPublisher.class);

    private static final String SERVICE_NAME = "PS";

    public PsPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishLog(String pattern, Object... args) {
        String formatted = format(pattern, args);

        String msg = "[" + SERVICE_NAME + "] " + formatted;

        logger.info(msg);

        rabbitTemplate.convertAndSend("log.queue", msg);
    }


    private String format(String pattern, Object... args) {
        if (args == null || args.length == 0) return pattern;

        StringBuilder sb = new StringBuilder();
        int index = 0;

        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '{' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '}') {
                if (index < args.length) {
                    sb.append(args[index++]);
                } else {
                    sb.append("{?}");
                }
                i++;
            } else {
                sb.append(pattern.charAt(i));
            }
        }

        return sb.toString();
    }
}
