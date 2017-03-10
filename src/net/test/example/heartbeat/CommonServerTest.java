package net.test.example.heartbeat;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import net.common.proto.protoc.BaseProtocol;
import net.server.CommonServer;
import net.server.handler.ServerHeartBeatHandler;
import net.server.handler.ServerIdleEventHandler;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanlia on 2017/1/5.
 * 服务端心跳处理器使用
 */
public class CommonServerTest {
    public static void main(String[] args){
        CommonServer server=new CommonServer();
        server.putChildChannelOption(ChannelOption.SO_BACKLOG,128);
        server.enableProps();
        server.putProps("serverPing",true);//server主动发起心跳
        int read=Integer.valueOf(server.getProps().getProperty("readIdleTime"));
        int write=Integer.valueOf(server.getProps().getProperty("writeIdleTime"));
        int all=Integer.valueOf(server.getProps().getProperty("allIdleTime"));
        server.addChildHandler("p1",new ProtobufVarint32FrameDecoder());
        server.addChildHandler("p2",new ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        server.addChildHandler("p3",new ProtobufVarint32LengthFieldPrepender());
        server.addChildHandler("p4",new ProtobufEncoder());
        server.addChildHandler("p5",new IdleStateHandler(read,write,all, TimeUnit.SECONDS));
        server.addChildHandler("p6",new ServerIdleEventHandler(server));
        server.addChildHandler("p7",new ServerHeartBeatHandler(server));
        server.createFatherPipeline();
        server.createChildPipeline();
        try{ server.bind(); } catch (InterruptedException e){ server.getLogger().error(e.getMessage()); }
    }
}
