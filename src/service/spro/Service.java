package service.spro;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by hanlia on 2017/1/11.
 * 每个服务有一个唯一的数字标识
 * 该数字标识由zookeeper分配
 * 当提供者将服务信息发布给zookeeper若注册成功则返回该标识数字
 * 服务属性包括
 * id=接口名
 * class=实现类名
 * url=IP_PORT
 * priority=服务等级
 * 服务端为一个netty server
 */
public class Service {
    //服务接口
    public Integer getInt(int local){
        return local*2;
    }

    //完成服务信息的构建
    public Object buildInfo(){
        ServiceMessage.Message.Builder builder= ServiceMessage.Message.newBuilder();
        builder.setId("Servicce");
        builder.setUrl("localhost_9898");
        builder.setClass_("Service");
        return builder.build();
    }

    //完成服务信息的编码和向zookeeper发送服务信息
    public void publish() throws Exception{
        ServiceMessage testMessage=new ServiceMessage();
        ProtobufEncoder encoder=new ProtobufEncoder(testMessage);
        ProtobufDecoder decoder=new ProtobufDecoder(testMessage);
        byte[]  result=encoder.encode(buildInfo());
        ZooKeeper zookeeper=new PublishService().connectZookeeper("localhost",2181);
        if(zookeeper.exists("/Service", false) != null)
        {
            zookeeper.delete("/Service",0);
            zookeeper.create("/Service", result, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        System.out.println(decoder.decode(zookeeper.getData("/Service",false,null)));
        zookeeper.close();
    }
    public void startService(){//启动一个netty server

    }

    public static void main(String[] args){
        Service service=new Service();
        try{ service.publish(); }catch (Exception e){}
    }
}
