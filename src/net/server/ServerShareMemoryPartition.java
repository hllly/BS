package net.server;

/**
 * Created by hanlia on 2017/1/6.
 */
public class ServerShareMemoryPartition {
    public static int len=4;

    public static int  serverReadIdleTimeSumStart=0;
    public static int  serverWriteIdleTimeSumStart=serverReadIdleTimeSumStart+len;
    public static int  serverNoReceivedPongTimesStart=serverWriteIdleTimeSumStart+len;
}
