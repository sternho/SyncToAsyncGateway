package org.sync.to.async.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sync.to.async.cache.CacheService;
import org.sync.to.async.dto.RequestObject;

import java.util.UUID;

@Slf4j
@Service
public class RabbitMqSender {

    private AmqpTemplate rabbitTemplate;

    private String routingKey = "amq.direct";
    private String queueName = "testing";

    @Autowired
    public RabbitMqSender(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(String queueName, String message) {
        log.info("sending: {}", message);
        rabbitTemplate.convertAndSend("amq.direct", queueName, message);
    }

    public void send(String message) {
        log.info("sending: {}", message);
        rabbitTemplate.convertAndSend("amq.direct", "testing", message);
    }

//    @PostConstruct
//    public void test() {
//        for(int i=0; i<10; i++) {
//            send("testing: " + i);
//        }
//    }

}
