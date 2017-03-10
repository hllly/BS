package net.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.client.CommonClient;
import net.common.handler.IdleEventHandler;
import net.common.handler.ShareMemory;
import net.client.ClientShareMemoryPartition;
import net.common.tool.CommonTool;
import net.common.tool.ShareMemoryIniter;

/**
 * Created by hanlia on 2017/1/3.
 * 客户端空闲事件处理Handler
 */
@ChannelHandler.Sharable
public class ClientIdleEventHandler extends IdleEventHandler{
    private CommonClient client;
    private ShareMemory sm=new ShareMemory("","");
    protected String id="localhost:9898";
    private int len=ClientShareMemoryPartition.len;
    public ClientIdleEventHandler(CommonClient client){
        this.client=client;
    }
    public ClientIdleEventHandler(CommonClient client,String id){this.client=client;this.id=id;}
    /**
     * 客户端心跳事件处理
     * 根据用户注册的IdleStateHandler检测出空闲类型做相应的处理
     * 若检测出最大时间空闲事件则累计客户端空闲时间
     * 当检测到最大空闲时间累计达到最大限值则关闭channel释放资源
     */
    @Override
    public void channelActive(ChannelHandlerContext context){
        this.sm=client.getSm();
    }
    @Override
    public  void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.sm=client.getSm();
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            int readIdleTime = Integer.valueOf(client.getProps().getProperty("readIdleTime"));
            int writeIdleTime = Integer.valueOf(client.getProps().getProperty("writeIdleTime"));
            int maxIdleTimeSum=Integer.valueOf(client.getProps().getProperty("IdleTime"));
            int NOReceivedPongTimes=Integer.valueOf(client.getProps().getProperty("NoReceivedPongTimes"));
            if (e.state() == IdleState.ALL_IDLE) {
                int clientReadIdleTimeSum = CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart, len, sm);
                clientReadIdleTimeSum += readIdleTime;
                CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart, len,clientReadIdleTimeSum,sm);

                int clientWriteIdleTimeSum =CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart, len, sm);
                clientWriteIdleTimeSum += writeIdleTime;
                CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart, len, clientWriteIdleTimeSum,sm);

                client.getLogger().info("client checked read and write idle.");
                ctx.write(createPingMessage(id));
                ctx.flush();
                client.getLogger().info("client send ping.");
            }
            else if (e.state() == IdleState.READER_IDLE) {
                int clientReadIdleTimeSum = CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart, len, sm);
                clientReadIdleTimeSum += readIdleTime;
                CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart, len, clientReadIdleTimeSum,sm);
                client.getLogger().info("client checked read idle.");
                ctx.write(createPingMessage(id));
                ctx.flush();
                client.getLogger().info("client send ping.");
            }
            else if (e.state() == IdleState.WRITER_IDLE) {
                int clientWriteIdleTimeSum =CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart, len, sm);
                clientWriteIdleTimeSum += writeIdleTime;
                CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart, len, clientWriteIdleTimeSum,sm);
                client.getLogger().info("client checked write idle.");
                //发起心跳ping
                ctx.write(createPingMessage(id));
                ctx.flush();
                client.getLogger().info("client send ping.");

            }
            int clientReadIdleTimeSum =CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart, len, sm);
            int clientWriteIdleTimeSum =CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart, len, sm);
            if (clientReadIdleTimeSum >= maxIdleTimeSum || clientWriteIdleTimeSum >= maxIdleTimeSum) {
                ctx.channel().close().sync();
                client.getLogger().info("channel is closed.");
            }
            } else {
                client.getLogger().info(((Exception) evt).getStackTrace());
            }
        }
}
