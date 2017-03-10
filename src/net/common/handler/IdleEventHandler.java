package net.common.handler;

import io.netty.channel.ChannelHandlerAdapter;
import net.common.proto.protoc.BaseProtocol;

/**
 * Created by hanlia on 2017/1/10.
 */
public class IdleEventHandler extends ChannelHandlerAdapter{
    /**
     * 创建ping消息实例
     * @return
     */
    public BaseProtocol.BaseMessage createPingMessage(String id){
        //发起心跳ping
        BaseProtocol.BaseMessage.Builder respBuilder = BaseProtocol.BaseMessage.newBuilder();
        respBuilder.setReqOrRespID(id);
        BaseProtocol.BaseMessage.Header.Builder headerBuilder = BaseProtocol.BaseMessage.Header.newBuilder();
        headerBuilder.setType(5);
        respBuilder.setHeader(headerBuilder.build());
        BaseProtocol.BaseMessage reqMessage = respBuilder.build();
        return reqMessage;
    }
}
