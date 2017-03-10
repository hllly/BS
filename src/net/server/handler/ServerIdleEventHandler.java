package net.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.common.handler.IdleEventHandler;
import net.common.handler.ShareMemory;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;
import net.common.tool.ShareMemoryIniter;
import net.server.ServerShareMemoryPartition;
import net.server.CommonServer;

/**
 * Created by hanlia on 2017/1/6.
 */
@ChannelHandler.Sharable
public class ServerIdleEventHandler extends IdleEventHandler{
    private CommonServer server;
    private ShareMemory sm=new ShareMemory("","");
    private String id="localhost:9898";
    private int len=ServerShareMemoryPartition.len;
    public ServerIdleEventHandler(CommonServer server){
        this.server=server;
    }
    public ServerIdleEventHandler(CommonServer server,String id){
        this.server=server;this.id=id;
    }
    /**
     * 根据用户注册的IdleStateHandler检测出空闲类型做相应的处理
     * 若检测出最大时间空闲事件则累计客户端空闲时间
     * 当检测到最大空闲时间累计达到最大限值则关闭channel释放资源
     */
    @Override
    public void channelActive(ChannelHandlerContext context){
        ShareMemoryIniter.loadSM(context,sm,"");
        ShareMemoryIniter.initServerSM(sm,ServerShareMemoryPartition.len);
    }

    @Override
    public  void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ShareMemoryIniter.loadSM(ctx,sm,"");
        if(server.getProps().getProperty("serverPing").equals("true")){
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                int readIdleTime = Integer.valueOf(server.getProps().getProperty("readIdleTime"));
                int writeIdleTime = Integer.valueOf(server.getProps().getProperty("writeIdleTime"));
                if(e.state() == IdleState.ALL_IDLE){
                    int clientReadIdleTimeSum =CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart, len, sm);
                    clientReadIdleTimeSum += readIdleTime;
                    CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart, len, clientReadIdleTimeSum,sm);
                    int clientWriteIdleTimeSum = CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart, len, sm);
                    clientWriteIdleTimeSum += writeIdleTime;
                    CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart, len, clientWriteIdleTimeSum,sm);
                    server.getLogger().info("server checked read and write idle.");
                    //发起心跳ping
                    ctx.write(createPingMessage(id));
                    ctx.flush();
                    server.getLogger().info("server send ping.");
                }
                else if (e.state() == IdleState.READER_IDLE) {
                    int clientReadIdleTimeSum = CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart, len, sm);
                    clientReadIdleTimeSum += readIdleTime;
                    CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart, len,clientReadIdleTimeSum,sm);
                    server.getLogger().info("client checked read idle.");
                    //发起心跳ping
                    ctx.write(createPingMessage(id));
                    ctx.flush();
                    server.getLogger().info("server send ping.");
                }
                else if (e.state() == IdleState.WRITER_IDLE) {
                    int clientWriteIdleTimeSum = CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart, len, sm);
                    clientWriteIdleTimeSum += writeIdleTime;
                    CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart, len,clientWriteIdleTimeSum,sm);
                    server.getLogger().info("client checked write idle.");
                    //发起心跳ping
                    ctx.write(createPingMessage(id));
                    ctx.flush();
                    server.getLogger().info("server send ping.");
                }
                int clientReadIdleTimeSum =CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart, len, sm);
                int clientWriteIdleTimeSum =CommonTool.readFromShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart, len, sm);
                int maxIdleTimeSum=Integer.valueOf(server.getProps().getProperty("maxIdleTimeSum"));
                if (clientReadIdleTimeSum >= maxIdleTimeSum || clientWriteIdleTimeSum >= maxIdleTimeSum) {
                    ctx.channel().close().sync();
                    server.getLogger().info("channel is closed.");
                }
            } else {
                server.getLogger().info(((Exception) evt).getStackTrace());
            }
        }
    }
}
