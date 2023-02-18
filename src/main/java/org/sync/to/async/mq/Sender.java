package org.sync.to.async.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Sender {

    private AmqpTemplate rabbitTemplate;

    private String routingKey = "amq.direct";
    private String queueName = "testing";

    @Autowired
    public Sender(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
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
