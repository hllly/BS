package net.client;

/**
 * Created by hanlia on 2017/1/6.
 * 共享内存块的划分
 * 即规定共享内存的某一段为存放某值
 * 当某条连接的处理线程需要读取或修改或写入时需按照分区进行操作
 */
public class ClientShareMemoryPartition {
    public static int len=4;

    public static int  connectClosedStart=0;
    public static int  reConnTimesStart=connectClosedStart+len;
    public static int  clientReadIdleTimeSumStart=reConnTimesStart+len;
    public static int  clientWriteIdleTimeSumStart=clientReadIdleTimeSumStart+len;
    public static int  clientNoReceivedPongTimesStart=clientWriteIdleTimeSumStart+len;
}
