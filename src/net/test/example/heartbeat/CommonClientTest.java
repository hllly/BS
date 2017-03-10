package net.test.example.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import net.client.ClientShareMemoryPartition;
import net.client.CommonClient;
import net.client.handler.ClientHeartBeatHandler;
import net.client.handler.ClientIdleEventHandler;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanlia on 2017/1/4.
 * 客户端心跳处理器使用
 */
public class CommonClientTest {
    public static void main(String[] args){
        CommonClient client=new CommonClient();
        client.enableProps();
        int read=Integer.valueOf(client.getProps().getProperty("readIdleTime"));
        int write=Integer.valueOf(client.getProps().getProperty("writeIdleTime"));
        int all=Integer.valueOf(client.getProps().getProperty("allIdleTime"));
        client.putChannelOption(ChannelOption.SO_KEEPALIVE,true);
        client.addHandler("p1",new ProtobufVarint32FrameDecoder());
        client.addHandler("p2",new ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        client.addHandler("p3",new ProtobufVarint32LengthFieldPrepender());
        client.addHandler("p4",new ProtobufEncoder());
        client.addHandler("idleStateHandler",new IdleStateHandler(read,write,all, TimeUnit.SECONDS));
        client.addHandler("ClientIdleEventHandler",new ClientIdleEventHandler(client));
        client.addHandler("ClientHeartBeatHandler",new ClientHeartBeatHandler(client));
        client.createPipeline(CommonTool.readFromShareMemory(ClientShareMemoryPartition.reConnTimesStart,ClientShareMemoryPartition.len,client.getSm()));
        client.connect(new Bootstrap());
    }
}
