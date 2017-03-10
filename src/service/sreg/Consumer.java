package service.sreg;

import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import net.client.ClientShareMemoryPartition;
import net.client.CommonClient;
import net.client.handler.ClientHeartBeatHandler;
import net.client.handler.ClientIdleEventHandler;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;
import net.common.tool.MessageType;
import java.util.*;
import java.util.concurrent.*;
/**
 * Created by hanlia on 2017/1/12.
 * 服务消费者实例 ，创建一个服务信息缓存，向服务注册中心订阅服务，将订阅服务信息刷新到本地，消费端定期获取刷新服务信息
 * 根据服务信息计算路由，发起远程调用
 */
public class Consumer {
    protected org.apache.log4j.Logger logger= org.apache.log4j.Logger.getLogger(BaseService.class);
    protected ServiceManager manager;
    protected HashMap serviceMap;
    protected String consumerName;

    public Consumer(ServiceManager manager){this.manager=manager;this.serviceMap=new HashMap();}
    public Consumer(ServiceManager manager,HashMap map){this.manager=manager;this.serviceMap=map;}

    //创建客户端
    public void createClient(String host,int port){
        CommonClient client=new CommonClient();
        client.putProps("maxRetries",5);
        client.putProps("workerGroupNum",3);
        client.putProps("host",host);
        client.putProps("port",port);
        client.enableProps();
        int read=Integer.valueOf(client.getProps().getProperty("readIdleTime"));
        int write=Integer.valueOf(client.getProps().getProperty("writeIdleTime"));
        int all=Integer.valueOf(client.getProps().getProperty("allIdleTime"));
        client.putChannelOption(ChannelOption.SO_BACKLOG,128);
        client.addHandler("ProtobufVarint32FrameDecoder",new ProtobufVarint32FrameDecoder());
        client.addHandler("ProtobufDecoder",new ProtobufDecoder(BaseProtocol.BaseMessage.getDefaultInstance()));
        client.addHandler("ProtobufVarint32LengthFieldPrepender",new ProtobufVarint32LengthFieldPrepender());
        client.addHandler("ProtobufEncoder",new ProtobufEncoder());
        client.addHandler("idleStateHandler",new IdleStateHandler(read,write,all, TimeUnit.SECONDS));
        client.addHandler("ConsumerHandler",new ConsumerHandler(this));
        client.createPipeline(CommonTool.readFromShareMemory(ClientShareMemoryPartition.reConnTimesStart,ClientShareMemoryPartition.len,client.getSm()));
        client.connect(new Bootstrap());
        assert client.getFuture().isDone();
        if(client.getFuture()==null){
            System.out.println("future is null.");
        }
        if(client.getFuture().isSuccess()){
            logger.info("client start successful.");
            try{
                client.getFuture().channel().closeFuture().sync();
            }catch (InterruptedException e){
                logger.error(e.getMessage());
            }finally {
                client.getFuture().channel().close();
                logger.info("client channel closed.");
            }
        }
        else {
            logger.error("client start failed.");
        }
    }

    //订阅服务
    public Map subscribeService(ArrayList<String> serviceNameList) throws Exception{
        HashMap serviceMap=new HashMap();
        for(String serviceName : serviceNameList){
            if(this.manager.queryService(serviceName)!=null)
                serviceMap.put(serviceName,this.manager.queryService(serviceName));
            else {
                this.logger.warn("not found service "+serviceName);
            }
        }
        this.serviceMap=serviceMap;
        return serviceMap;
    }

    //定期刷新本地服务缓存，使用Callable定期刷新缓存
    public boolean flushServiceCache(int interval,String host,int port) throws Exception{
        ExecutorService service= Executors.newSingleThreadExecutor();
        while(true){
            logger.info("start to flush service cache.");
            try{
                Thread.sleep(interval);
                //查询结果为HashMap，本地缓存为HashMap
                Future<HashMap> future=
                        service.submit(new Callable<HashMap>() {
                            @Override
                            public HashMap call() {
                                ServiceManager manager=null;
                                HashMap result=new HashMap();
                                try {
                                    manager = new ServiceManager(host,port);
                                    result=manager.queryAllService(host, port,consumerName);
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                }
                                return result;
                            }
                        });
                assert future.isDone();
                try{
                    if(future.get()!=null){
                        serviceMap.clear();
                        serviceMap=future.get();
                        logger.info("service cache flush is completed.");
                    }else {
                        logger.info("no service to flush.");
                    }
                }catch (ExecutionException e){
                    logger.error(e.getMessage());
                }
            }catch (InterruptedException e){
                logger.error("cannot flush service cache.");
            }
        }
    }

    //计算路由
    public String getURL(){
        return "localhost_9898";
    }

    //构建远程调用parameter
    public BaseProtocol.BaseMessage buildRequestMessage(){
        BaseProtocol.BaseMessage.Builder builder=BaseProtocol.BaseMessage.newBuilder();
        BaseProtocol.BaseMessage.Header.Builder headerBuilder=BaseProtocol.BaseMessage.Header.newBuilder();
        BaseProtocol.BaseMessage.Body.Builder bodyBuilder=BaseProtocol.BaseMessage.Body.newBuilder();
        BaseProtocol.BaseMessage.Body.Request.Builder requestBuilder=BaseProtocol.BaseMessage.Body.Request.newBuilder();
        BaseProtocol.BaseMessage.Body.Response.Builder responseBuilder=BaseProtocol.BaseMessage.Body.Response.newBuilder();

        headerBuilder.setType(MessageType.BusReqMsg);//设置消息类型
        headerBuilder.setPriority(5);//设置消息优先级
        headerBuilder.addInterfaceName("TestService");//设置远程调用接口
        headerBuilder.addMethodName("getRemoteInt");//设置远程调用方法

        requestBuilder.addMethodParametersType("Integer");//设置方法参数类型
        int para=10;
        requestBuilder.addMethodParameters(ByteString.copyFrom(CommonTool.intToBytes(para)));//设置方法参数

        bodyBuilder.setRequest(requestBuilder.build());//构建消息体
        bodyBuilder.setResponse(responseBuilder.build());//构建消息头

        builder.setHeader(headerBuilder.build());//构建完整消息
        builder.setBody(bodyBuilder.build());//构建完整消息

        return builder.build();
    }

    //构建响应消息
    public BaseProtocol.BaseMessage buildResponseMessage(){
        return null;
    }

    //发起远程调用
    public <T> T invoke(BaseProtocol.BaseMessage message){
        return null;
    }



    public static void main(String[] args){
        try{
            Consumer consumer=new Consumer(new ServiceManager("localhost",2181));
            ArrayList<String > service=new ArrayList<>();
            service.add("TestService");
            consumer.subscribeService(service);
            consumer.createClient("localhost",9898);
        }catch (Exception e){
        }
    }
}
