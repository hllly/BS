package net.test.example.baseuse;

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
import net.common.handler.RuntimeReconnectorHandler;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanlia on 2017/1/5.
 * 基本的Client用例
 */
public class CommonClientTest {
    public static void main(String[] args){
        CommonClient client=new CommonClient();
        client.putProps("host","localhost");
        client.putProps("port",9898);
        client.enableProps();
        client.putChannelOption(ChannelOption.SO_BACKLOG,128);
        client.addHandler("ProtobufVarint32FrameDecoder",new ProtobufVarint32FrameDecoder());
        client.addHandler("ProtobufDecoder",new ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        client.addHandler("ProtobufVarint32LengthFieldPrepender",new ProtobufVarint32LengthFieldPrepender());
        client.addHandler("ProtobufEncoder",new ProtobufEncoder());
        client.addHandler("IdleStateHandler",new IdleStateHandler(5,5,5, TimeUnit.SECONDS));
        client.addHandler("ClientHeartBeatHandler",new ClientHeartBeatHandler(client));
        client.addHandler("RuntimeReconnectorHandler",new RuntimeReconnectorHandler(client));
        client.createPipeline(CommonTool.readFromShareMemory(ClientShareMemoryPartition.reConnTimesStart,ClientShareMemoryPartition.len,client.getSm()));
        client.connect(new Bootstrap());
    }
}
