package org.sync.to.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.sync.to.async.webserver.HttpServer;

@EnableAsync
@SpringBootApplication
public class SyncToAsyncGatewayApplication {

    @Autowired
    HttpServer httpServer;

    public static void main(String[] args) {
        SpringApplication.run(SyncToAsyncGatewayApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startNettyServer() throws Exception {
        httpServer.run();
    }

}
