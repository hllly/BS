package net.test.example.baseuse;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import net.common.proto.protoc.BaseProtocol;
import net.server.CommonServer;
import net.server.handler.ServerHeartBeatHandler;

/**
 * Created by hanlia on 2017/1/5.
 * CommonServer基本用例
 */
public class CommonServerTest {
    public static void main(String[] args){
        CommonServer server=new CommonServer();
        server.enableProps();
        server.addChildHandler("ProtobufVarint32FrameDecoder",new ProtobufVarint32FrameDecoder());
        server.addChildHandler("ProtobufDecoder",new ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        server.addChildHandler("ProtobufVarint32LengthFieldPrepender",new ProtobufVarint32LengthFieldPrepender());
        server.addChildHandler("ProtobufEncoder",new ProtobufEncoder());
        server.addChildHandler("p6",new ServerHeartBeatHandler(server));
        server.createFatherPipeline();
        server.createChildPipeline();
        server.putChildChannelOption(ChannelOption.SO_BACKLOG,128);
        try{ server.bind(); } catch (InterruptedException e){ server.getLogger().error(e.getMessage()); }
    }
}
