package net.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.client.CommonClient;
import net.common.handler.BaseHeartBeatHandler;
import net.common.handler.ShareMemory;
import net.common.proto.protoc.BaseProtocol;
import net.client.ClientShareMemoryPartition;
import net.common.tool.CommonTool;
/**
 * Created by hanlia on 2017/1/5.
 * 客户端心跳处理handler
 * 当链路读空闲空闲或写空闲时发起心跳ping
 * 当收到心跳ping时回复心跳pong
 */
@ChannelHandler.Sharable
public class ClientHeartBeatHandler extends BaseHeartBeatHandler {
    private CommonClient client;
    private ShareMemory sm;
    public ClientHeartBeatHandler(CommonClient client){
        this.client=client;
    }

    @Override
    public void channelActive(ChannelHandlerContext context){
        try{
            this.sm=client.getSm();
            super.channelActive(context);
            client.getLogger().info("client is active.");
        }catch (Exception e){
            client.getLogger().info(e.getMessage());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        this.sm=client.getSm();
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        if(baseMessage.getHeader().getType()==5){
            try{
                client.getLogger().info("client received ping.");
                super.channelRead(ctx,message);
                client.getLogger().info("client send pong.");
            }catch (Exception e){
                client.getLogger().error(e.getMessage());
            }
        }else if(baseMessage.getHeader().getType()==6){
            Integer data=0;
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientNoReceivedPongTimesStart,ClientShareMemoryPartition.len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart,ClientShareMemoryPartition.len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart,ClientShareMemoryPartition.len,data,sm);
            client.getLogger().info("client received pong.");
        }
    }
}
