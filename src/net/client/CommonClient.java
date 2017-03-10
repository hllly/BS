package net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.common.handler.ShareMemory;
import net.common.tool.CommonTool;
import net.common.tool.ShareMemoryIniter;
import org.apache.log4j.Logger;

/**
 * Created by hanlianlian on 2016/12/23.
 * 该类为继承自BaseClient的通用客户端引导类
 * 该类实现客户端实例化参数的参数化配置
 * @props 为客户端实例基本参数集合，该字段在CommClient构造器里加载客户端属性文件client.properties
 * @workerGroup 客户端EventLoop类，用于处理客户端SocketChannel
 * @channelInitializer 用于设置客户端SocketChannel Handler容器ChannelPipeline
 * @channelHandlerList 用于暂存用户设置的ChannelHandler，在创建ChannelPipeline时被加载到ChannelPipeline
 * @bootstrap 客户端辅助引导类
 *
 *
 * 客户端的的remote host和port为一个数组，客户端可以并发的发出一组连接请求
 */
public class CommonClient implements  BaseClient {
    private static Logger logger = Logger.getLogger(CommonClient.class);
    private Properties props;
    private NioEventLoopGroup workerGroup;
    private ChannelInitializer<SocketChannel> channelInitializer;
    private ArrayList<HashMap<String, ChannelHandler>> channelHandlerList;
    private Bootstrap bootstrap;
    private ChannelFuture future=null;
    private ShareMemory sm;
    /**
     * 添加客户端Tcp连接参数
     * 可添加多个Tcp参数
     * 所有Tcp参数均来自Netty ChannelOption
     * 该参数将被直接添加到bootstrap.option()
     * T 表示添加的Tcp参数类型
     * @option Tcp参数类型
     * @value 实参值
     */
    public <T> Bootstrap putChannelOption(ChannelOption<T> option, T value) {
        bootstrap.option(option, value);
        return this.bootstrap;
    }

    /**
     * 添加客户端ChannelHandler
     * 该参数首先被添加到channelHandlerList
     * 然后在createPipeline()方法里添加到ChannelPipeline
     */
    public Bootstrap addHandler(String handlerName, ChannelHandler handler) {
        HashMap<String, ChannelHandler> handlerMap = new HashMap<>();
        handlerMap.put(handlerName, handler);
        channelHandlerList.add(handlerMap);
        return this.bootstrap;
    }

    /**
     * 创建channelInitializer
     * 即ChannelHandler容器ChannelPipeline所在类实例
     * ChannelHandler首先被加载到ChannelPipeline
     * 最后在bootstrap引导服务端时被加载到.handler()
     * times表示连接次数，当为0则表示初次连接，当大于0则表示重连
     */
    public Bootstrap createPipeline(Integer times) {
            this.channelInitializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    for (Map<String, ChannelHandler> outHandler : channelHandlerList)
                        for (String handlerName : outHandler.keySet()) {
                            if(ch.pipeline().get(handlerName)==null)
                                ch.pipeline().addLast(handlerName, outHandler.get(handlerName));
                        }
                }
            };
        return this.bootstrap;
    }

    /**
     * 用户可以根据逻辑策略在业务处理时在ChannelPipeline上动态添加或移除ChannelHandler
     * 当用户动态添加或移除ChannelHandler时调用此方法更新ChannelPipeline
     */
    public void reloadPipeline(int times) {
        createPipeline(times);
    }

    /**
     * 启用用户动态设置的props参数
     * 即通过putProps()动态设置的参数
     * 如果动态设置的参数和客户端初始化类构造器里加载的配置参数重复
     * 则使用用户动态参数覆盖初始化时设置的参数
     * 参数详细说明：
     * 以及根据相关参数实例化workerGroup
     */
    public void enableProps() {
        int workerGroupNum = Integer.valueOf(props.get("workerGroupNum").toString());
        if (workerGroupNum >= 1 && workerGroupNum <= ClientPropsOption.MAX_WORKERGROUPNUM)
            workerGroup = new NioEventLoopGroup(workerGroupNum);
        else
            workerGroup = new NioEventLoopGroup();
        String shareFileName=props.getProperty("host")+"_"+props.getProperty("port");
        this.sm=new ShareMemory(shareFileName,"");
        ShareMemoryIniter.initClientSM(sm,ClientShareMemoryPartition.len);
    }

    /**
     * 用户动态配置客户端基本参数方法
     * 该方法直接将用户设置的参数以key-value形式放到props
     */
    public Bootstrap putProps(Object key, Object value) {
        props.put(key, value);
        return this.bootstrap;
    }

    /**
     * CommonClient构造器
     * 包括实例化channelHandlerList，props，bootstrap
     * 并将client.properties配置参数加载到props
     */
    public CommonClient() {
        this.channelHandlerList = new ArrayList<>();
        this.bootstrap = new Bootstrap();
        this.props = new Properties();
        try {
            String filePath = System.getProperty("user.dir") + "\\src\\net\\common\\property\\client.properties";
            InputStream input = new FileInputStream(filePath);
            this.props.load(input);
            logger.info("loaded client.properties.");
        } catch (IOException e) {
            logger.error("client.properties load error.");
        }
    }

    /**
     * 使用bootstrap引导客户端并发起远程连接
     * 引导时加载所有tcp参数，props参数，handler
     * 参数设置完毕后发起异步连接
     * 若连接失败或超时则发起重连
     * 当达到最大重连次数并仍然连接失败关闭连接释放资源
     * 当超过最大空闲累和则主动关闭连接释放资源
     */
    @Override
    public void connect(Bootstrap bootstrap) {
        this.workerGroup=(NioEventLoopGroup) workerGroup;
        bootstrap.group(this.workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(this.channelInitializer);
        String host = this.getProps().get("host").toString();
        int port = Integer.valueOf(this.getProps().get("port").toString());
        this.future = bootstrap.connect(host, port).addListener(new ConnectionListener(this));
    }

    public void shutdown(){
        this.future.channel().close();
        logger.info("channel is closed.");
        this.workerGroup.shutdownGracefully();
        logger.info("workerGroup is shutdown.");
    }

    public String getLocalIP() throws Exception{
        InetAddress addr = InetAddress.getLocalHost();
        String ip=addr.getHostAddress().toString();//获得本机IP
        return ip;
    }

    public String getLocalID(ChannelFuture future){
        return CommonTool.splitID(future.channel().localAddress().toString());
    }

    public String getShareFileName(String localHost,int localPort,String remoteHost,int remotePort){
        return localHost+"_"+localPort+"@"+remoteHost+"_"+remotePort;
    }

    /**
     * getter
     */
    public Properties getProps(){return props;}
    public Bootstrap getBootstrap(){return bootstrap;}
    public Logger getLogger(){ return logger; }
    public NioEventLoopGroup getWorkerGroup(){return workerGroup;}
    public ChannelFuture getFuture(){return future;}
    public void setFuture(ChannelFuture future){this.future=future;}
    public ShareMemory getSm(){ return sm; }

    public void loadSM(ChannelHandlerContext context){

    }
}
