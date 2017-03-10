package service.sreg;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.MessageType;

/**
 * Created by hanlia on 2017/1/13.
 * 用户处理服务调用
 * 当接收到消息时该Handler首先判断是否为响应消息
 * 若是则从消息里获取结果
 */
public class ConsumerHandler extends ChannelHandlerAdapter{
    private Consumer consumer;
    public ConsumerHandler(Consumer consumer){this.consumer=consumer;}

    @Override
    public void channelActive(ChannelHandlerContext context){
        System.out.println("client is active.");
        context.write(consumer.buildRequestMessage());
        System.out.println(consumer.buildRequestMessage());
        context.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext context,Object msg){
        BaseProtocol.BaseMessage message=(BaseProtocol.BaseMessage)msg;
        if(message.getHeader().getType()== MessageType.BusRespMsg){}
    }
}
