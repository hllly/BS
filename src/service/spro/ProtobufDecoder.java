package service.spro;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created by hanlia on 2017/1/10.
 * 用于解码自定义协议
 * 将bytes解码成响应的协议类型
 * 需要将message泛型化
 */
public class ProtobufDecoder {
    private BaseProtoMessage OutMessage;
    public ProtobufDecoder(BaseProtoMessage message){
        OutMessage=message;
    }
    public  Object decode(byte[] message) throws InvalidProtocolBufferException{
        return OutMessage.decode(message);
    }
}
