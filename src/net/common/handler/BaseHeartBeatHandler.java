package net.common.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.common.proto.protoc.BaseProtocol;
/**
 * Created by hanlia on 2017/1/5.
 * 心跳处理基本Handler
 * 提供发起心跳ping的方法
 * 提供回复心跳pong的方法
 */
public class BaseHeartBeatHandler extends ChannelHandlerAdapter{
    protected String id="localhost:9898";
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        //5代表心跳ping
        if(baseMessage.getHeader().getType()==5){
            BaseProtocol.BaseMessage.Builder reqBuilder=BaseProtocol.BaseMessage.newBuilder();
            reqBuilder.setReqOrRespID(id);
            BaseProtocol.BaseMessage.Header.Builder headerBuilder=BaseProtocol.BaseMessage.Header.newBuilder();
            headerBuilder.setType(6);
            reqBuilder.setHeader(headerBuilder.build());
            BaseProtocol.BaseMessage reqMessage=reqBuilder.build();
            ChannelFuture f=ctx.write(reqMessage);
            ctx.flush();
        }
    }

    /**
     * 每发送一次心跳ping则将计数器增加1，每收到一次心跳pong则将计数器置为0
     * 当计数器大于设定的限值则抛出自定义异常
     * @param context
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context,Throwable cause){}

    public String setReqOrRespID(String id){this.id=id; return id;}
}
