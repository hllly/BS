package net.util.traffic;

import io.netty.handler.traffic.AbstractTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

/**
 * Created by hanlia on 2016/12/30.
 * 自定义的流量整形Handler
 * 除自定义的流量整形Handler外系统提供
 * 全局流量整形（父类Handler）GlobalTrafficShapingHandler
 * 链路流量整形（子类childHandler）ChannelTrafficShapingHandler
 */
public class ServerTrafficShapingHandler extends AbstractTrafficShapingHandler{
    @Override
    public void doAccounting(TrafficCounter counter){
        //to do here
    }
}
