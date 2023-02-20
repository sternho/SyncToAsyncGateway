package org.sync.to.async.cache;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CacheService {

    private Map<String, ChannelHandlerContext> data = new HashMap<>();

    public void push(String id, ChannelHandlerContext ctx) {
        data.put(id, ctx);
    }

    public ChannelHandlerContext get(String id) {
        return data.get(id);
    }

}
