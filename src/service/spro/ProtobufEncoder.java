package service.spro;

/**
 * Created by hanlia on 2017/1/10.
 * 用于编码自定义协议
 * 将协议序列化成bytes
 */
public class ProtobufEncoder {
    private BaseProtoMessage OutMessage;
    public ProtobufEncoder(BaseProtoMessage message){
        OutMessage=message;
    }
    public  byte[] encode(Object message){
        return OutMessage.encode(message);
    }
}
