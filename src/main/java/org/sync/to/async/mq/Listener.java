package org.sync.to.async.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Listener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        log.info("received: " + new String(message.getBody()));
    }
}
