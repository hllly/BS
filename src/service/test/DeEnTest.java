package service.test;

import com.google.protobuf.InvalidProtocolBufferException;
import service.ser.BaseProtoMessage;
import service.ser.ProtobufDecoder;
import service.ser.ProtobufEncoder;
import service.ser.protoc.TestMessage;

/**
 * Created by hanlia on 2017/1/10.
 */
public class DeEnTest {
    public static void main(String[] args){
        TestMessage testMessage=new TestMessage();
        ProtobufEncoder encoder=new ProtobufEncoder(testMessage);
        ProtobufDecoder decoder=new ProtobufDecoder(testMessage);
        TestMessage.Message.Builder builder= TestMessage.Message.newBuilder();
        builder.setTest("hello world.");
        try{
            System.out.println(decoder.decode(encoder.encode(builder.build())));
        }catch (InvalidProtocolBufferException e){}
    }
}
