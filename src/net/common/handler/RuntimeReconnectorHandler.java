package net.common.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.client.CommonClient;

/**
 * Created by hanlia on 2017/1/5.
 * 客户端运行时断连重连Handler
 * 当客户端与服务端建立的正常连接因故断开时发起连接
 */
@ChannelHandler.Sharable
public class RuntimeReconnectorHandler extends ChannelHandlerAdapter{
    private CommonClient client;
    public RuntimeReconnectorHandler(CommonClient client){
        this.client=client;
    }
    @Override
    public void channelInactive(ChannelHandlerContext context){
        client.connect(new Bootstrap());
    }
}
