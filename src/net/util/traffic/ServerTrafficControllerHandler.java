package net.util.traffic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.AbstractTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

/**
 * Created by hanlia on 2016/12/30.
 * 流控Handler
 * 该功能配合消息优先级处理
 * 当消息接收速率过高则选择抛弃部分优先级较低的任务
 */
public class ServerTrafficControllerHandler extends AbstractTrafficShapingHandler{
    @Override
    public void doAccounting(TrafficCounter counter){

    }

    public void channelRead(ChannelHandlerContext ctx){}
}
