package service.javassist;

import java.util.ArrayList;
/**
 * Created by hanlia on 2017/1/15.
 * 用来缓存代理类
 */
public class CompoundKeyWeakHashMap<K,V,M> {
    private ArrayList<Cache> cacheList;
    private Cache<ClassLoader,Class,Class> cache;

    public CompoundKeyWeakHashMap(){
        this.cache=new Cache();
        this.cacheList=new ArrayList<>();
    }

    public void put(ClassLoader classLoader,Class targetClass,Class proxyClass){
        this.cache.setClassLoader(classLoader);
        this.cache.setTargetClass(targetClass);
        this.cache.setProxyClass(proxyClass);
        this.cacheList.add(cache);
    }

    public Class get(ClassLoader classLoader,Class targetClass){
        Class result=null;
        for(int i=0;i<this.cacheList.size();i++){
            Cache<ClassLoader,Class,Class> cla1=this.cacheList.get(i);
            if(cla1.getTargetClass().getName().equals(targetClass.getName())){
                result=cla1.getProxyClass();
            }
        }
        return result;
    }
}
