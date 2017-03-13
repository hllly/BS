package net.util.heartbeat;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;

/**
 * Created by hanlia on 2016/12/26.
 * 该类用于当用户在服务端使用Netty ReadTimeoutHandler或WriteTimeoutHandler是检测到超时的处理机制
 */
public class ServerTimeoutCaughtHandler extends ChannelHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof WriteTimeoutException) {
            ctx.channel().close();
            System.out.println("服务器写超时，服务器已关闭连接！");
        } else if (cause instanceof ReadTimeoutException) {
            ctx.channel().close();
            System.out.println("服务器读超时，服务器已关闭连接！");
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
