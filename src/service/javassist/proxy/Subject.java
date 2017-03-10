package service.javassist.proxy;

/**
 * Created by hanlia on 2017/1/16.
 * 需要被代理的对象需要实现的接口
 */
public interface Subject {
    void rent();
    String hello(String str);
}
