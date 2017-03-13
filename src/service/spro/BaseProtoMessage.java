package service.spro;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created by hanlia on 2017/1/10.
 * 所有 协议类型的基类
 * 使用protoc生成协议对应类之后让该类实现BaseProtoMessage
 * 并使用生存类的toArrayBytes()实现encode()
 * 使用parseFrom()实现decode()方法
 */
public interface BaseProtoMessage {
    byte[] encode(Object message);
    <T> T decode(byte[] message) throws InvalidProtocolBufferException;
}
