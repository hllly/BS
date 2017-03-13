package net.util.priority;

import net.proto.protoc.BaseProtocol;
import net.server.CommonServer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by hanlia on 2016/12/29.
 * 消息队列轮询算法
 * 根据消息队列1-10的优先级按比例获取message
 * 按照1+2+3+4+5+6+7+8+9+10取消息
 */
public class PriorityQueuePoller {
    private CommonServer server;
    private ArrayList<BaseProtocol.BaseMessage> handlingMessageQueue=new ArrayList<>();
    public PriorityQueuePoller(CommonServer server){
        this.server=server;
    }
    public void poll() throws InterruptedException{
        ExecutorService service= Executors.newSingleThreadExecutor();
        Future<ArrayList<BaseProtocol.BaseMessage>> result=service.submit(
                new Callable<ArrayList<BaseProtocol.BaseMessage>>() {
                    @Override
                    public ArrayList<BaseProtocol.BaseMessage> call(){
                        return getMessage(server.getPriorityMessageQueue());
                    }
        });
        try{
            this.handlingMessageQueue.addAll(result.get());
        }catch (ExecutionException e){
        }
        Thread.sleep(1);//一秒钟轮询一次
}


    public ArrayList<BaseProtocol.BaseMessage> getMessage(Map<Integer,ArrayList<BaseProtocol.BaseMessage>> messageMap){
        Set pris=server.getPriorityMessageQueue().keySet();
        ArrayList<BaseProtocol.BaseMessage> result=new ArrayList<>();
        for(Object pri : pris){
            Integer key=(Integer)pri;
            int size=server.getPriorityMessageQueue().get(key).size();
            int len=0;
            if(size>key) len=key;
            else len=size;
            for(int i=0;i<len;i++){
                result.add(server.getPriorityMessageQueue().get(key).get(i));
            }
        }
        return result;
    }

    public ArrayList<BaseProtocol.BaseMessage> getHandlingMessageQueue(){
        return handlingMessageQueue;
    }
}
