package net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import net.common.handler.ShareMemory;
import net.common.tool.CommonTool;
import java.util.concurrent.TimeUnit;
/**
 * Created by hanlia on 2017/1/4.
 * 客户端连接监听器
 * 当客户端连接失败时发起重连
 */
public class ConnectionListener implements ChannelFutureListener {
    private CommonClient client;
    private ShareMemory sm;
    public ConnectionListener(CommonClient client) {
        this.client = client;
    }
    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        //连接取消
        int len=ClientShareMemoryPartition.len;
        this.sm=client.getSm();

        if(channelFuture.isCancelled()){
            client.getLogger().info("connect is cancelled.");
        }
        //连接成功后将connected属性置为true，将reConnTimes置为0，将connectClosed置为0
        else if(channelFuture.isSuccess()){
            Integer reConnTimes=0;
            Integer connectClosed=0;
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.reConnTimesStart,len,reConnTimes,this.sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.connectClosedStart,len,connectClosed,this.sm);
            client.getLogger().info("connect is successful.");
        }
        //连接失败判断已重连次数，如果超过最大重连次数则关闭连接并释放资源，若没有则继续重连
        else if (!channelFuture.isSuccess()) {
            int reConnTimes=CommonTool.readFromShareMemory(ClientShareMemoryPartition.reConnTimesStart,len,this.sm);
            int maxRetries=Integer.valueOf(client.getProps().getProperty("maxRetries"));
            if(reConnTimes>maxRetries){
                client.shutdown();
                client.getLogger().info("connect is failed finally, client is shutdown.");
            }
            else{
                Long interval=Long.valueOf(client.getProps().getProperty("retryInterval"));
                client.getLogger().info("connect failed and retry.");
                final EventLoop loop = channelFuture.channel().eventLoop();
                reConnTimes+=1;
                CommonTool.writeToShareMemory(ClientShareMemoryPartition.reConnTimesStart,len,reConnTimes,this.sm);
                loop.schedule(new Runnable() {
                    @Override
                    public void run() {
                        client.connect(new Bootstrap());
                    }
                }, interval, TimeUnit.SECONDS);
            }
            }
    }
}