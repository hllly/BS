package net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.common.proto.protoc.BaseProtocol;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by hanlia on 2017/1/5.
 */
public class CommonServer implements BaseServer{
    /**
     * 服务端需要实例化如下字段：
     * props
     * IDInfo
     * bossGroup
     * workerGroup
     * fatherChannelHandlerList
     * childChannelHandlerList
     * fatherChannelInitializer
     * childChannelInitializer
     * props是系统默认参数和用户动态指定参数的配置容器
     * IDInfo是客户端发起请求时携带 的自身ID信息
     * channelHandlerList是用户动态指定的ChannelHandler，默认没有ChannelHandler
     */
    protected Properties props;
    protected Map<String, String[]> IDInfo;
    protected NioEventLoopGroup bossGroup;
    protected NioEventLoopGroup workerGroup;
    protected ChannelInitializer<ServerSocketChannel> fatherChannelInitializer;
    protected ChannelInitializer<SocketChannel> childChannelInitializer;
    protected ArrayList<HashMap<String,ChannelHandler>> fatherChannelHandlerList;
    protected ArrayList<HashMap<String,ChannelHandler>> childChannelHandlerList;
    protected ServerBootstrap serverBootstrap;
    protected Map<Integer,ArrayList<BaseProtocol.BaseMessage>> priorityMessageQueue;
    protected ChannelFuture childChannelFuture=null;
    protected Logger logger=Logger.getLogger(CommonServer.class);

    /**
     * 构造器定义
     * 包括实例化除bossGroup和workerGroup外的所有字段
     * 在构造器里将server.properties配置参数加载到props
     */
    public CommonServer(){
        this.IDInfo = new HashMap<>();
        this.fatherChannelHandlerList=new ArrayList<>();
        this.childChannelHandlerList=new ArrayList<>();
        this.serverBootstrap=new ServerBootstrap();
        this.props=new Properties();
        try{
            String filePath=System.getProperty("user.dir")+"\\src\\net\\common\\property\\server.properties";
            InputStream input=new FileInputStream(filePath);
            this.props.load(input);
            // log here
        }catch (IOException e){
            // log here
        }
    }

    /**
     * 用户动态配置参数方法
     * 所有参数的名称以String形式放在ServerParameters里
     */
    public ServerBootstrap putProps(Object key,Object value){
        props.put(key,value);
        return this.serverBootstrap;
    }

    /**
     * bossGroup和workerGroup的实例创建
     * 以及启用用户设置的props参数
     * 即通过putProps()动态设置的参数
     * 如果动态设置的参数和服务端初始化类构造器里加载的配置参数重复
     * 则使用用户动态参数覆盖初始化时设置的参数
     * 参数详细说明：
     *
     */
    public void enableProps(){
        int bossGroupNum=Integer.valueOf(props.get("bossGroupNum").toString());
        int workerGroupNum=Integer.valueOf(props.get("workerGroupNum").toString());
        if(bossGroupNum >=1 && bossGroupNum <=ServerPropsOption.MAX_BOSSGROUPNUM)
            bossGroup=new NioEventLoopGroup(bossGroupNum);
        else
            bossGroup=new NioEventLoopGroup(1);
        if(workerGroupNum>=1 && workerGroupNum<=ServerPropsOption.MAX_WORKERGROUPNUM)
            workerGroup=new NioEventLoopGroup(workerGroupNum);
        else
            workerGroup=new NioEventLoopGroup();
        if(props.getProperty("priorityMessageQueue").equals("true")){
            this.priorityMessageQueue=new ConcurrentHashMap<>();
        }
    }

    /**
     * 添加父类Tcp连接参数
     * 可添加多个Tcp参数
     * 所有Tcp参数均来自Netty ChannelOption
     * 该参数直接添加到serverBootstrap.option()
     */
    public <T> ServerBootstrap putChannelOption(ChannelOption<T> option, T value){
        serverBootstrap.option(option,value);
        return this.serverBootstrap;
    }

    /**
     * 添加子类Tcp连接参数
     * 可添加多个Tcp参数
     * 所有Tcp参数均来自Netty ChannelOwegh -=ption
     * 该参数直接添加到serverBootstrap.childOption()
     */
    public <T> ServerBootstrap putChildChannelOption(ChannelOption<T> option,T vaule){
        serverBootstrap.childOption(option,vaule);
        return this.serverBootstrap;
    }

    /**
     * 添加fatherHandler
     * 该参数首先被添加到HashMap<String,ChannelHandler> fatherChannelHandlerList
     * 然后在createFatherPipeline()方法里添加到父类ChannelPipeline
     */
    public ServerBootstrap addFatherHandler(String handlerName,ChannelHandler handler){
        HashMap<String,ChannelHandler> handlerMap=new HashMap<>();
        handlerMap.put(handlerName,handler);
        fatherChannelHandlerList.add(handlerMap);
        return this.serverBootstrap;
    }
    /**
     * 添加childHandler
     * 该参数首先被添加到HashMap<String,ChannelHandler> childChannelHandlerList
     * 然后在createChildPipeline()方法里添加到子类ChannelPipeline
     */
    public ServerBootstrap addChildHandler(String handlerName,ChannelHandler handler){
        HashMap<String,ChannelHandler> handlerMap=new HashMap<>();
        handlerMap.put(handlerName,handler);
        childChannelHandlerList.add(handlerMap);
        return this.serverBootstrap;
    }

    /**
     * 创建fatherChannelInitializer
     * 即fatherHandler容器
     * 父类的ChannelHandler首先被加载到fatherPipeline
     * 最后在serverBootstrap引导服务端时被加载到Handler
     */
    public ServerBootstrap createFatherPipeline(){
        this.fatherChannelInitializer=new ChannelInitializer<ServerSocketChannel>() {
            @Override
            protected void initChannel(ServerSocketChannel ch) throws Exception {
                for(Map<String,ChannelHandler> outHandler : fatherChannelHandlerList)
                    for(String handlerName : outHandler.keySet())
                        ch.pipeline().addLast(handlerName,outHandler.get(handlerName));
            }
        };
        return this.serverBootstrap;
    }

    /**
     * 创建childChannelInitializer
     * 即childHandler容器
     * 子类的ChannelHandler首先被加载到childPipeline
     * 最后在serverBootstrap引导服务端时被加载childHandler
     */
    public  ServerBootstrap createChildPipeline(){
        this.childChannelInitializer=new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                for(Map<String,ChannelHandler> outHandler : childChannelHandlerList)
                    for(String handlerName : outHandler.keySet())
                        ch.pipeline().addLast(handlerName,outHandler.get(handlerName));
            }
        };
        return this.serverBootstrap;
    }

    /**
     * 用户可以根据逻辑策略在业务处理时动态添加或移除ChannelHandler
     * 当用户动态改变handler时调用此方法更新ChannelPipeline
     */
    public void reloadFatherPipeline(){
        createFatherPipeline();
    }
    public void reloadChildPipeline(){
        createChildPipeline();
    }

    /**
     * 在服务端与客户端建立新连接或退出连接时调用
     * 该IDInfo存储的是客户端发起请求连接时携带的自身ID信息
     * 该IDInfo的作用包括IP黑白名单验证和握手保护
     * IDInfo内容包括host，port，连接次数，黑白属性
     * 该IDInfo可以动态添加、删除、修改
     * 当连接关闭时需要将连接次数置为0
     * 当连接建立时需要将连接次数置为1
     * 黑白属性为0时为白名单即允许连接
     * 黑白属性为1时为黑名单即拒绝连接
     * 初次建立连接时默认可连接
     * host:port组成一个ID字串作为key
     */
    public void addIDInfo(Map<String, String[]> ID){
        this.IDInfo.putAll(ID);
    }
    public void removeIDInfo(String key){
        this.IDInfo.remove(key);
    }
    public void setIDInfoConnTimes(String socket,int times){ this.IDInfo.get(socket)[0]=times+""; }
    public void setIDInfoWhiteOrBlack(String socket,boolean whiteOrBlack){ this.IDInfo.get(socket)[1]=whiteOrBlack+""; }
    public void setIDInfo(String ID,String[] info){
        Map<String,String[]> clientInfo=new HashMap<>();
        clientInfo.put(ID,info);
        this.IDInfo.put(ID,info);
    }
    /**
     * 使用serverBootstrap引导服务端并绑定端口
     * 引导时加载所有tcp参数，props参数，handler等
     * 参数设置完毕后断点端口并监听
     */
    @Override
    public void bind()throws InterruptedException{
        serverBootstrap.group(this.bossGroup,this.workerGroup).channel(NioServerSocketChannel.class);
        serverBootstrap.handler(this.fatherChannelInitializer);
        serverBootstrap.childHandler(this.childChannelInitializer);
        int port=Integer.valueOf(props.get("port").toString());
        ChannelFuture future=serverBootstrap.bind(port).sync();
        assert future.isDone();
        if(future.isSuccess()){
            this.childChannelFuture=future;
            logger.info("server start successful!");
        }else{
            logger.info("server start failed!");
        }
    }

    public void shutdown(){
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }

    /**
     * 所有字段getter器
     */
    public Properties getProps(){return props;}
    public Map<String,String[]> getIDInfo(){ return IDInfo; }
    public Logger getLogger(){return logger;}
    public Map<Integer,ArrayList<BaseProtocol.BaseMessage>> getPriorityMessageQueue(){return priorityMessageQueue;}
    public ChannelFuture getChildChannelFuture(){return childChannelFuture;}
}
