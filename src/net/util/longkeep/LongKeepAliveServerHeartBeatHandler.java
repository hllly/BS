package net.util.longkeep;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.proto.protoc.BaseProtocol;

/**
 * Created by hanlia on 2016/12/29.
 * 当使用长连接时服务端的心跳处理
 * 主要包括发起心跳和接收回应心跳
 * 当链路空闲时则发起心跳
 * 若接收的心跳是5则回复6
 * 若接收的心跳是6则不做事
 */
public class LongKeepAliveServerHeartBeatHandler extends ChannelHandlerAdapter{
    @Override
    public  void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state()== IdleState.READER_IDLE  || e.state()== IdleState.WRITER_IDLE ){


                System.out.println("server test idle userEvent");


                BaseProtocol.BaseMessage.Builder respBuilder=BaseProtocol.BaseMessage.newBuilder();
                respBuilder.setReqOrRespID("localhost:9898@localhost:9898");
                BaseProtocol.BaseMessage.Header.Builder headerBuilder=BaseProtocol.BaseMessage.Header.newBuilder();
                headerBuilder.setType(5);
                respBuilder.setHeader(headerBuilder.build());
                BaseProtocol.BaseMessage respMessage=respBuilder.build();
                ctx.write(respMessage);
                ctx.flush();
            }else {
                super.userEventTriggered(ctx,evt);
            }
        }
        else{}
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        System.out.println("服务端读数据！");
        //5代表心跳ping
        if(baseMessage.getHeader().getType()==5){

            System.out.println(baseMessage);
            System.out.println("server test idle channelRead");

            BaseProtocol.BaseMessage.Builder reqBuilder=BaseProtocol.BaseMessage.newBuilder();
            reqBuilder.setReqOrRespID("localhost:9898@localhost:9898");
            BaseProtocol.BaseMessage.Header.Builder headerBuilder=BaseProtocol.BaseMessage.Header.newBuilder();
            headerBuilder.setType(6);
            reqBuilder.setHeader(headerBuilder.build());
            BaseProtocol.BaseMessage reqMessage=reqBuilder.build();
            ctx.write(reqMessage);
            ctx.flush();
        }
        else if(baseMessage.getHeader().getType()==6){
            System.out.println("客户端工作正常！");
        }
    }
}
