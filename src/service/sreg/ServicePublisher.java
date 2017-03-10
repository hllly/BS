package service.sreg;

import org.apache.zookeeper.ZooKeeper;
import service.ser.BaseProtoMessage;
/**
 * Created by hanlia on 2017/1/11.
 */
public class ServicePublisher {
    public ZooKeeper connectZookeeper(String host, int port) throws Exception{
        ZooKeeper zookeeper = new ZooKeeper(host, 3000, null);
        return zookeeper;
    }
    public void sendToZookeeper(BaseProtoMessage message){}
}
