package net.util.priority;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.proto.protoc.BaseProtocol;
import net.server.CommonServer;

import java.util.ArrayList;

/**
 * Created by hanlia on 2016/12/29.
 * 消息优先级处理Handler
 * 将消息按优先级分类并放入队列
 */
public class ServerPriorityHandler extends ChannelHandlerAdapter{
    private CommonServer server;
    public ServerPriorityHandler(CommonServer server){
        this.server=server;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object message){
        BaseProtocol.BaseMessage baseMessage=(BaseProtocol.BaseMessage)message;
        if(server.getProps().getProperty("priorityMessageQueue").equals("true")){
            int maxValue=Integer.valueOf(server.getProps().getProperty("priorityMaxValue"));
            int priority;
            if(baseMessage.getHeader().getPriority()>maxValue || baseMessage.getHeader().getPriority()<1)
                priority=10;
            else priority=baseMessage.getHeader().getPriority();
            if(server.getPriorityMessageQueue().get(priority) != null){
                server.getPriorityMessageQueue().get(priority).add(baseMessage);
            }else {
                ArrayList<BaseProtocol.BaseMessage> messageList=new ArrayList<>();
                messageList.add(baseMessage);
                server.getPriorityMessageQueue().put(priority,messageList);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        PriorityQueuePoller poller=new PriorityQueuePoller(this.server);
        try{
            while(true) {
                poller.poll();
            }
        }catch (Exception e){}
    }
}
