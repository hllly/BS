package net.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.common.handler.BaseHeartBeatHandler;
import net.common.handler.ShareMemory;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;
import net.common.tool.ShareMemoryIniter;
import net.server.ServerShareMemoryPartition;
import net.server.CommonServer;

/**
 * Created by hanlia on 2017/1/5.
 * 服务端心跳处理Handler
 * 当监测到链路读空闲或写空闲时发送心跳ping
 * 如用户开启服务端心跳ping
 * 则当接收到客户端心跳ping时回复pong
 */
@ChannelHandler.Sharable
public class ServerHeartBeatHandler extends BaseHeartBeatHandler{
    private CommonServer server;
    private ShareMemory sm;
    public ServerHeartBeatHandler(CommonServer server){
        this.server=server;
    }

    @Override
    public void channelActive(ChannelHandlerContext context){
        try{
            ShareMemoryIniter.loadSM(context,sm,"");
            super.channelActive(context);
            server.getLogger().info("server is active.");
        }catch (Exception e){
            server.getLogger().error(e.getMessage());
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        //5代表心跳ping
        if(baseMessage.getHeader().getType()==5){
            try{
                server.getLogger().info("server received ping.");
                super.channelRead(ctx,message);
                server.getLogger().info("server send pong.");
            }catch (Exception e){
                server.getProps().getProperty(e.getMessage());
            }
        }
        else if(baseMessage.getHeader().getType()==6){
            server.getLogger().info("server received pong.");
            int data=0;
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverNoReceivedPongTimesStart,ServerShareMemoryPartition.len,data,sm);
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart,ServerShareMemoryPartition.len,data,sm);
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart,ServerShareMemoryPartition.len,data,sm);
        }
    }
}
