package org.sync.to.async.webserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.sync.to.async.cache.CacheService;
import org.sync.to.async.dto.RequestObject;
import org.sync.to.async.mq.RabbitMqSender;

import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
@Scope("prototype")
@Component
public class AsyncHttpServerHandler extends SimpleChannelInboundHandler<Object> {

    @Autowired
    private RabbitMqSender mqSender;
    @Autowired
    private CacheService cacheService;

    private StringBuilder responseData = new StringBuilder();
    private HttpRequest request;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                writeResponse(ctx);
            }

            responseData.setLength(0);
            responseData.append(RequestUtils.formatParams(request));
        }

        responseData.append(RequestUtils.evaluateDecoderResult(request));

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            final var uri = request.uri();
            final var queueName = uri.substring(uri.indexOf("api/"));
            final var requestBody = RequestUtils.formatBody(httpContent).toString();


            ObjectMapper mapper = new ObjectMapper();
            final var reqObj = new RequestObject(UUID.randomUUID().toString(), requestBody);
            final var json = mapper.writeValueAsString(reqObj);
            cacheService.push(reqObj.id, ctx);
            mqSender.send("testing", json);


//            responseData.append(RequestUtils.formatBody(httpContent));
//            responseData.append(RequestUtils.evaluateDecoderResult(request));
//
//            if (msg instanceof LastHttpContent) {
//                LastHttpContent trailer = (LastHttpContent) msg;
//                responseData.append(RequestUtils.prepareLastResponse(request, trailer));
//                writeResponse(ctx, trailer, responseData);
//            }
        }
    }

    private static void writeResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String responseData) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, status,
                Unpooled.copiedBuffer(responseData, CharsetUtil.UTF_8));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.write(httpResponse);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    private void writeResponse(ChannelHandlerContext ctx, LastHttpContent trailer, StringBuilder responseData) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1,
                trailer.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(httpResponse);

        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
