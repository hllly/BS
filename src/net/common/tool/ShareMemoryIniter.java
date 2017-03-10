package net.common.tool;

import io.netty.channel.ChannelHandlerContext;
import net.client.ClientShareMemoryPartition;
import net.common.handler.ShareMemory;
import net.server.ServerShareMemoryPartition;

/**
 * Created by hanlia on 2017/1/10.
 */
public class ShareMemoryIniter {
    //公共初始化方法
    public static void initSM(ShareMemory sm,int len,String who){
        int data=0;
        sm.setInitFlag(true);
        if(who.equals("client")){
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.reConnTimesStart,len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart,len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.connectClosedStart,len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart,len,data,sm);
            CommonTool.writeToShareMemory(ClientShareMemoryPartition.clientNoReceivedPongTimesStart,len,data,sm);
        }
        else{
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverNoReceivedPongTimesStart,len,data,sm);
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverReadIdleTimeSumStart,len,data,sm);
            CommonTool.writeToShareMemory(ServerShareMemoryPartition.serverWriteIdleTimeSumStart,len,data,sm);
        }
    }
    //client初始化方法
    public static void initClientSM(ShareMemory sm,int len){
        initSM(sm,len,"client");
    }
    //server初始化方法
    public static void initServerSM(ShareMemory sm,int len){
        initSM(sm,len,"server");
    }
    //客户端初次初始化


    //使用context自带的di加载
    public static void loadSM(ChannelHandlerContext context,ShareMemory sm,String sharePath){
        String sourceID= CommonTool.splitID(context.channel().localAddress().toString());
        String goalID=CommonTool.splitID(context.channel().remoteAddress().toString());
        if(sharePath.equals(""))
            sm=new ShareMemory(sourceID+"@"+goalID,"");
        else
            sm=new ShareMemory(sourceID+"@"+goalID,sharePath);
    }
    //使用用户指定id加载
    public static void loadSM(ChannelHandlerContext context,ShareMemory sm,String sourceID,String goalID,String sharePath){
        if(sourceID.equals("") || goalID.equals("")){
            sourceID= CommonTool.splitID(context.channel().localAddress().toString());
            goalID=CommonTool.splitID(context.channel().remoteAddress().toString());
        }
        if(sharePath.equals(""))
            sm=new ShareMemory(sourceID+"@"+goalID,"");
        else
            sm=new ShareMemory(sourceID+"@"+goalID,sharePath);
    }

    public static void testClientSM(ShareMemory sm){
        int len=ClientShareMemoryPartition.len;
        System.out.println("----NOreceivedPing:"+Integer.valueOf(CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientNoReceivedPongTimesStart,len,sm)));
        System.out.println("----readTime:"+Integer.valueOf(CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientReadIdleTimeSumStart,len,sm)));
        System.out.println("----connectClosed:"+Integer.valueOf(CommonTool.readFromShareMemory(ClientShareMemoryPartition.connectClosedStart,len,sm)));
        System.out.println("----reConnTime:"+Integer.valueOf(CommonTool.readFromShareMemory(ClientShareMemoryPartition.reConnTimesStart,len,sm)));
        System.out.println("----WriteIdleTime:"+Integer.valueOf(CommonTool.readFromShareMemory(ClientShareMemoryPartition.clientWriteIdleTimeSumStart,len,sm)));
    }

    public static void main(String[] args){
        ShareMemory sm=new ShareMemory("","");
        initClientSM(sm,ClientShareMemoryPartition.len);
        loadSM(null,sm,"");
        testClientSM(sm);
    }
}
