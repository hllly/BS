package net.common.proto.protoc;

/**
 * Created by hanlianlian on 2016/12/8.
 * 用于将proto文件编译成java class
 */
public class ProtoBuilder {
    /**
     * args[0]为proto编译器路径
     * args[1]为被编译proto文件路径
     * args[2]为编译后的消息类输出路径
     * args[3]为proto文件路径+proto文件名
     * @param args
     */
    public void buildMessageClass(String[] args){
        Runtime rn=Runtime.getRuntime();
        Process p=null;
        try{
            p=rn.exec(args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ProtoBuilder protoBuilder=new ProtoBuilder();
        String[] arg=new String[4];
        arg[0] = "C:\\Users\\hanlia\\Desktop\\bishe\\pro\\bin\\protoc.exe";
        arg[1] = "--proto_path=C:\\Users\\hanlia\\Desktop\\bishe\\pro\\BaseService\\src\\net\\common\\proto\\protof";
        arg[2] = "--java_out=C:\\Users\\hanlia\\Desktop\\bishe\\pro\\BaseService\\src\\net\\common\\proto\\protoc";
        arg[3] = "C:\\Users\\hanlia\\Desktop\\bishe\\pro\\BaseService\\src\\net\\common\\proto\\protof\\BaseProtocol.proto";
        protoBuilder.buildMessageClass(arg);
    }
}
