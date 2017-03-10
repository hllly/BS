package service.sreg;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import net.common.proto.protoc.BaseProtocol;
import net.server.CommonServer;
import net.server.handler.ServerHeartBeatHandler;
import net.server.handler.ServerIdleEventHandler;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import service.ser.ProtobufDecoder;
import service.ser.ProtobufEncoder;
import service.ser.protoc.BaseServiceProtocol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanlia on 2017/1/12.
 * 服务管理类
 * 用于管理zookeeper的服务注册
 * 包括定期向Consumer推送变化
 * 接收消费者客户端的连接
 * 处理服务信息增删改查
 */
public class ServiceManager {
    protected ZooKeeper zooKeeper;

    public ServiceManager(String host,int port) throws Exception{
        this.zooKeeper=new ServicePublisher().connectZookeeper(host,port);
    }

    //服务删除
    public boolean delete(ArrayList<String> serviceNameList) throws Exception{
        ZooKeeper zooKeeper=new ServicePublisher().connectZookeeper("localhost",9898);
        zooKeeper.delete(getPath("TestService"),1);
        if(zooKeeper.exists(getPath("TestService"),false)==null)
            return true;
        else
            return false;
    }

    //服务更新
    public boolean update(){
        return true;
    }

    //单个服务查询
    public BaseServiceProtocol.BaseMessage queryService(String serviceName) throws Exception{
        byte[] result=zooKeeper.getData(getPath("TestService"),false,null);
        ProtobufDecoder decoder=new ProtobufDecoder(new BaseServiceProtocol());
        return (BaseServiceProtocol.BaseMessage)decoder.decode(result);
    }

    //所有服务查询
    public HashMap queryAllService(String host,int port,String consumerName)throws Exception{
        ProtobufDecoder decoder=new ProtobufDecoder(new BaseServiceProtocol());
        HashMap resultMap=new HashMap();
        List<String> serviceList=zooKeeper.getChildren("/Service/"+consumerName,false);
        for(String name : serviceList){
            BaseServiceProtocol.BaseMessage message=(BaseServiceProtocol.BaseMessage)decoder.decode(zooKeeper.getData("/Service/"+name,false,null));
            resultMap.put(message.getId(),message);
        }
        return resultMap;
    }

    //服务注册
    public boolean register(BaseServiceProtocol.BaseMessage message,String name)throws Exception{
        ProtobufEncoder encoder=new ProtobufEncoder(new BaseServiceProtocol());
        byte[] serviceInfo=encoder.encode(message);
        zooKeeper.create("/Service/"+name,serviceInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if(zooKeeper.exists("Service"+name,false)==null){ return false; }
        return true;
    }

    //启动管理器，监听消费者连接
    public void startServiceManager(String host,int port){
        CommonServer server=new CommonServer();
        server.putChildChannelOption(ChannelOption.SO_BACKLOG,128);
        server.putProps("serverPing",false);
        server.putProps("host",host);
        server.putProps("port",port);
        server.enableProps();
        int read=Integer.valueOf(server.getProps().getProperty("readIdleTime"));
        int write=Integer.valueOf(server.getProps().getProperty("writeIdleTime"));
        int all=Integer.valueOf(server.getProps().getProperty("allIdleTime"));
        server.addChildHandler("ProtobufVarint32FrameDecoder",new ProtobufVarint32FrameDecoder());
        server.addChildHandler("ProtobufDecoder",new io.netty.handler.codec.protobuf.ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        server.addChildHandler("ProtobufVarint32LengthFieldPrepender",new ProtobufVarint32LengthFieldPrepender());
        server.addChildHandler("ProtobufEncoder",new io.netty.handler.codec.protobuf.ProtobufEncoder());
        server.addChildHandler("IdleStateHandler",new IdleStateHandler(read,write,all, TimeUnit.SECONDS));
        server.addChildHandler("ServerIdleEventHandler",new ServerIdleEventHandler(server));
        server.addChildHandler("ServerHeartBeatHandler",new ServerHeartBeatHandler(server));
        server.createFatherPipeline();
        server.createChildPipeline();
        try{ server.bind(); } catch (InterruptedException e){ server.getLogger().error(e.getMessage()); }
    }

    //推送变化，在服务信息的增删该查后注册一个回调，当增删该查完成后再回调里选择消费者推送信息
    public boolean notifyConsumer(){return true;}

    public String getPath(String serviceName){
        return "/"+serviceName;
    }
}
