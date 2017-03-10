package net.common.tool;

import com.google.protobuf.ByteString;
import net.common.handler.ShareMemory;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by hanlia on 2017/1/6.
 */
public class CommonTool {
    public static Logger logger=Logger.getLogger(CommonTool.class);
    public static String splitID(String id){
        String source=id.split("/")[1];
        String sourceID=source.split(":")[0]+"_"+source.split(":")[1];
        return sourceID;
    }

    public static void writeToShareMemory(int start, int len, int data,ShareMemory sm){
        sm.write(start,len,intToBytes(data));
    }

    public static int readFromShareMemory(int start,int len,ShareMemory sm){
        return bytesToInt(start,len,sm);
    }
    public static Integer parseString(String str){
        for(int i=0;i<1024;i++){
            if(str.equals(i+""))
                return i;
        }
        return null;
    }

    public static int bytesToInt(int start,int len,ShareMemory sm){
        byte[] data=new byte[len];
        sm.read(start,len,data);
        int value = (int) ((data[0] & 0xFF)<<24)
                | ((data[1] & 0xFF)<<16)
                | ((data[2] & 0xFF)<<8)
                | ((data[3] & 0xFF));
        return value;
    }

    public static int bytesToInt(byte[] data){
        int value = (int) ((data[0] & 0xFF)<<24)
                | ((data[1] & 0xFF)<<16)
                | ((data[2] & 0xFF)<<8)
                | ((data[3] & 0xFF));
        return value;
    }

    public static byte[] intToBytes(int data){
        byte[] result=new byte[4];
        result[0]=(byte)((data>>24)&0xFF);
        result[1] =  (byte) ((data>>16) & 0xFF);
        result[2] =  (byte) ((data>>8) & 0xFF);
        result[3] =  (byte) (data & 0xFF);
        return result;
    }

    public static byte[] byteStringToByteArray(ByteString byteString){
        return byteString.toByteArray();
    }

    public static void main(String[] args){
        Integer test=1000;
        byte[] data=intToBytes(test);
        System.out.println("byte[]:"+data);
        byte[] test1=new byte[data.length];
        //首先将数据转成ByteBuffer,
        // 然后将ByteBuff转成ByteString传输，
        // 之后再将ByteString转成ByteBuffer，
        // 最后将ByteBuffer转成数据
        //行不通
        ByteBuffer buffer=ByteBuffer.wrap(data);
        ByteString bytes=ByteString.copyFrom(buffer.array());
        System.out.println("result:"+bytesToInt(bytes.toByteArray()));
        System.out.println();

    }
}
