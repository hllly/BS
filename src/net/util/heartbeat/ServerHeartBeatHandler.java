package net.util.heartbeat;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.proto.protoc.BaseProtocol;

/**
 * Created by hanlia on 2016/12/26.
 * 服务端心跳监测
 * channelRead()用来响应ping
 */
public class ServerHeartBeatHandler extends ChannelHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage= (BaseProtocol.BaseMessage) message;
        if(baseMessage.getHeader().getType()==5){
            System.out.println(baseMessage);
            BaseProtocol.BaseMessage.Builder reqBuilder=BaseProtocol.BaseMessage.newBuilder();
            reqBuilder.setReqOrRespID("localhost:9898");
            BaseProtocol.BaseMessage.Header.Builder headerBuilder=BaseProtocol.BaseMessage.Header.newBuilder();
            headerBuilder.setCrcCode(10);
            headerBuilder.setType(6);
            reqBuilder.setHeader(headerBuilder.build());
            BaseProtocol.BaseMessage reqMessage=reqBuilder.build();
            ctx.write(reqMessage);
            ctx.flush();
        }
    }
}
