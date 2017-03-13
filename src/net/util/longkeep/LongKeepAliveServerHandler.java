package net.util.longkeep;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.proto.protoc.BaseProtocol;
import net.server.SocketChannelMap;

/**
 * Created by hanlia on 2016/12/29.
 * 长连接服务端处理Handler
 * 注册在服务端childHandler
 * 当客户端退出连接则移除
 * 当客户端发起连接时若SocketChannelMap里没有该客户端ID
 * 则将该客户端SocketChannel加入SocketChannelMap
 * 其中黑白名单的验证交给父类Handler去处理
 */
public class LongKeepAliveServerHandler extends ChannelHandlerAdapter{
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannelMap.remove((SocketChannel)ctx.channel());
        ctx.channel().close().sync();
    }
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {
        BaseProtocol.BaseMessage baseMsg = (BaseProtocol.BaseMessage) message;
        String clientID=baseMsg.getReqOrRespID().split("@")[0];
        if(SocketChannelMap.get(clientID)==null){
            SocketChannelMap.add(clientID,(NioSocketChannel)channelHandlerContext.channel());
        }
    }
}
