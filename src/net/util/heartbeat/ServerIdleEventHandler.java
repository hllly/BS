package net.util.heartbeat;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by hanlia on 2016/12/26.
 * 用来处理服务端空闲事件
 * 主要包括链路检测和心跳处理
 */
public class ServerIdleEventHandler extends ChannelHandlerAdapter{
    /**
     * 根据用户注册的IdleStateHandler检测出空闲类型做相应的处理
     * 若检测出最大时间空闲事件则
     * 若当前服务端为CommonServer则服务端主动关闭连接
     * 若当前服务端为LongKeepAliveServer则主动发起心跳若没有回应则关闭连接
     */
    @Override
    public  void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception{
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE ) {
                ctx.channel().close();
                System.out.println("超过最大空闲时间，服务器已关闭连接！");
            } else if(e.state() == IdleState.WRITER_IDLE ){
                //write and read all idle to do something
                ctx.channel().close();
                System.out.println("超过最大空闲时间，服务器已关闭连接！");
            }else if(e.state()==IdleState.ALL_IDLE){
                ctx.channel().close();
                System.out.println("超过最大空闲时间，服务器已关闭连接！");
            }else {
                super.userEventTriggered(ctx,evt);
            }
        }
        else{
            // do not know what ha
        }
    }
}
