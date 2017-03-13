package service.spro;

import org.apache.zookeeper.ZooKeeper;

/**
 * Created by hanlia on 2017/1/11.
 * 将服务信息发给zookeeper
 */
public class PublishService {
    public ZooKeeper connectZookeeper(String host,int port) throws Exception{
        ZooKeeper zookeeper = new ZooKeeper(host, 3000, null);
        return zookeeper;
    }
    public void sendToZookeeper(BaseProtoMessage message){}
}
