package service.sreg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import net.common.proto.protoc.BaseProtocol;
import net.server.CommonServer;
import net.server.handler.ServerHeartBeatHandler;
import net.server.handler.ServerIdleEventHandler;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import service.ser.ProtobufDecoder;
import service.ser.ProtobufEncoder;
import service.ser.protoc.BaseServiceProtocol;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanlia on 2017/1/11.
 * 服务类
 * 包含对服务信息的构建方法
 * 服务信息的编码和发送给zookeeper方法
 */
public class BaseService implements Service{
    protected Logger logger=Logger.getLogger(BaseService.class);

    @Override
    public Object buildInfo(){
        BaseServiceProtocol.BaseMessage.Builder builder=BaseServiceProtocol.BaseMessage.newBuilder();
        builder.setId("TestService");
        builder.setUrl("localhost_9898");
        builder.setClass_("TestService");
        return builder.build(); }

    @Override
    public void publish(String host,int port,String path) throws Exception{
        BaseServiceProtocol baseServiceProtocol=new BaseServiceProtocol();
        ProtobufEncoder encoder=new ProtobufEncoder(baseServiceProtocol);
        ProtobufDecoder decoder=new ProtobufDecoder(baseServiceProtocol);
        byte[]  result=encoder.encode(buildInfo());
        ZooKeeper zookeeper=new ServicePublisher().connectZookeeper(host,port);
        if(zookeeper.exists(path, false) == null)
        {
            zookeeper.create(path, result, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }else {
            logger.error(path+"is exist.");
        }
        System.out.println(decoder.decode(zookeeper.getData(path,false,null)));
        zookeeper.close();
    }

    @Override
    public void startService(String host,int port){
        CommonServer server=new CommonServer();
        server.putChildChannelOption(ChannelOption.SO_BACKLOG,128);
        server.enableProps();
        server.putProps("serverPing",false);
        server.putProps("host",host);
        server.putProps("port",port);
        int read=Integer.valueOf(server.getProps().getProperty("readIdleTime"));
        int write=Integer.valueOf(server.getProps().getProperty("writeIdleTime"));
        int all=Integer.valueOf(server.getProps().getProperty("allIdleTime"));
        server.addChildHandler("ProtobufVarint32FrameDecoder",new ProtobufVarint32FrameDecoder());
        server.addChildHandler("ProtobufDecoder",new io.netty.handler.codec.protobuf.ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        server.addChildHandler("ProtobufVarint32LengthFieldPrepender",new ProtobufVarint32LengthFieldPrepender());
        server.addChildHandler("ProtobufEncoder",new io.netty.handler.codec.protobuf.ProtobufEncoder());
        server.addChildHandler("IdleStateHandler",new IdleStateHandler(read,write,all, TimeUnit.SECONDS));
        server.addChildHandler("BaseServiceHandler",new BaseServiceHandler());
        server.createFatherPipeline();
        server.createChildPipeline();
        try{
            server.bind();
            ChannelFuture future=server.getChildChannelFuture();
            try{
                future.channel().closeFuture().sync();
            }catch (InterruptedException e){
                logger.error(e.getMessage());
            }finally {
                future.channel().close();
                logger.info("server current child channel closed.");
            }
        } catch (InterruptedException e){ server.getLogger().error(e.getMessage()); }
    }


    public Logger getLogger(){return logger;}

    public static void main(String[] args){
        BaseService service=new BaseService();
        try{
            service.publish("localhost",2181,"/TestService");
            service.startService("localhost",9898);
        }catch (Exception e){
            service.getLogger().error(e.getMessage());
        }
    }
}
