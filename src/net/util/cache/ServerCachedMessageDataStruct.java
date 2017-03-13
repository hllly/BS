package net.util.cache;

import net.proto.protoc.BaseProtocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hanlia on 2016/12/27.
 * 服务端缓存重发消息数据结构
 * 使用目的ID作为Map<key,value>的key
 * 将所有该目的ID的数组作为value
 */
public class ServerCachedMessageDataStruct {
    public static Map<String,ArrayList<BaseProtocol.BaseMessage>> cacheMessageMap=new ConcurrentHashMap<>();
    public static void writeToServerCache(BaseProtocol.BaseMessage baseMessage){
        String key=getGoalID(baseMessage);
        if(cacheMessageMap.get(key) != null){
            cacheMessageMap.get(key).add(baseMessage);
        }else {
            ArrayList<BaseProtocol.BaseMessage> value=new ArrayList<>();
            value.add(baseMessage);
            cacheMessageMap.put(key,value);
        }
    }

    public static ArrayList<BaseProtocol.BaseMessage> getCacheMessageList(String goalID){
        if(cacheMessageMap.get(goalID)==null){
            return null;
        }else {
            ArrayList<BaseProtocol.BaseMessage> result=new ArrayList<>();
            result=cacheMessageMap.get(goalID);
            cacheMessageMap.remove(goalID);
            return result;
        }
    }

    public static String getGoalID(BaseProtocol.BaseMessage baseMessage){
        String ID=baseMessage.getReqOrRespID();
        String goalID=ID.split("@")[1];
        return goalID;
    }
}
