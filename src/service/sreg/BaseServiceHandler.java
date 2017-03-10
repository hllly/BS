package service.sreg;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.common.proto.protoc.BaseProtocol;
import net.common.tool.CommonTool;
import net.common.tool.MessageType;
import service.ser.ProtobufDecoder;
import service.ser.protoc.BaseServiceProtocol;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
/**
 * Created by hanlia on 2017/1/13.
 * 用于处理远程调用
 * 消费端发送消息到该Handler
 * 该Handler判断是否为远程调用
 * 若是则解析服务调用参数
 */
public class BaseServiceHandler extends ChannelHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext context){
    }

    @Override
    public void channelRead(ChannelHandlerContext context,Object message){
        BaseProtocol.BaseMessage msg=(BaseProtocol.BaseMessage)message;
        System.out.println(msg);
        System.out.println("parameters:"+ CommonTool.bytesToInt(msg.getBody().getRequest().getMethodParameters(0).toByteArray()));
        if(msg.getHeader().getType()== MessageType.BusReqMsg || ((BaseProtocol.BaseMessage) message).getHeader().getType()==MessageType.BusReqAndRespMsg){
            ProtobufDecoder decoder=new ProtobufDecoder(new BaseServiceProtocol());
            try{
                String serviceName=msg.getHeader().getInterfaceName(0);//获取服务名
                System.out.println("interface:"+serviceName);

                Class<?> service= Class.forName("service.sreg."+serviceName);//获取服务类
                System.out.println("service:"+service.getName());

                String methodName=msg.getHeader().getMethodName(0);//获取方法名
                System.out.println("method name:"+methodName);

                String parameterTypes=msg.getBody().getRequest().getMethodParametersType(0);//获取参数类型名
                System.out.println("parameterTypeName:"+parameterTypes);

                Class<?> parameterType=Class.forName("java.lang."+parameterTypes);//获取参数类型
                System.out.println("parameterType:"+parameterType.getName());

                ByteString parameter=msg.getBody().getRequest().getMethodParameters(0);//获取参数
                System.out.println("parameter:"+CommonTool.bytesToInt(parameter.toByteArray()));

                Method method=service.getMethod(methodName,parameterType); //根据参数类型和方法名从找到的服务里找出方法
                System.out.println("method name:"+method.getName());
                Object result=method.invoke(service.newInstance(),CommonTool.bytesToInt(parameter.toByteArray()));//构建服务实例并执行找到的方法

            }catch (Exception e){
            }
        }
    }
}
