package org.sync.to.async.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sync.to.async.cache.CacheService;
import org.sync.to.async.dto.RequestObject;
import org.sync.to.async.webserver.AsyncHttpServerHandler;

@Slf4j
@Service
public class RabbitMqListener implements MessageListener {

    @Autowired
    private CacheService cacheService;

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        final var body = new String(message.getBody());
        log.info("received: {}", body);

        ObjectMapper mapper = new ObjectMapper();
        final var obj = mapper.readValue(body, RequestObject.class);
        final var ctx = cacheService.get(obj.id);
        if(ctx!=null) {
            AsyncHttpServerHandler.writeResponse(ctx, HttpResponseStatus.OK, body);
        } else {
            log.warn("[{}] Session not found", obj.id);
        }
    }
}
