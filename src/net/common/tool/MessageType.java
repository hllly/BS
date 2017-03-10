package net.common.tool;

import service.ser.protoc.TestMessage;

/**
 * Created by hanlia on 2017/1/12.
 * 存放消息类型
 */
public class MessageType {
    public static int BusReqMsg=10;//业务请求
    public static int BusRespMsg=1;//业务响应
    public static int BusReqAndRespMsg=2;//业务请求和响应
    public static int ShkReqMsg=3;//握手请求
    public static int ShkRespMsg=4;//握手响应
    public static int PingMsg=5;//ping消息
    public static int PongMsg=6;//pong消息
    public static int ContinueMsg=7;//续连消息
}
