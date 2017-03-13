package net.util.cache;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.proto.protoc.BaseProtocol;
import net.server.CommonServer;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by hanlia on 2016/12/27.
 * 当用户启用消息缓存重发时使用
 * 当链路联通以后，客户端会在channelActive()里发送续连信息
 * 服务端在channelRead()里判断是否为续连信息
 * 若为续连，则服务端首先去共享内存寻找该I目的ID为该客户端的缓存消息并取出重新发送
 */

/**
 * 客户端和服务端用法说明：
 */
public class ServerCachedRetryHandler extends ChannelHandlerAdapter{
    private CommonServer server;
    public ServerCachedRetryHandler(CommonServer server){
        this.server=server;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx){
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        if(baseMessage.getHeader().getType()==7){
            ArrayList<BaseProtocol.BaseMessage> baseMessageList=new ArrayList<>();
            Set<String> goalIDSet=this.server.getIDInfo().keySet();
            String goalID=null;
            for(String id : goalIDSet){
                goalID=id;
            }
            baseMessageList= ServerCachedMessageDataStruct.getCacheMessageList(goalID);
            if(baseMessageList==null){
                return;
            }
            for(BaseProtocol.BaseMessage retryMessage : baseMessageList){
                ChannelFuture future=ctx.write(retryMessage);
                ctx.flush();
                assert future.isDone();
                if(!future.isSuccess() || future.isCancelled()){
                    ServerCachedMessageDataStruct.writeToServerCache(retryMessage);
                }
            }
        }
    }
}
