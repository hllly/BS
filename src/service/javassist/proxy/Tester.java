package service.javassist.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by hanlia on 2017/1/16.
 */
public class Tester {
    public static void main(String[] args){
        Subject subject=new RealSubject();
        InvocationHandler handler=new DynamicProxy(subject);
        //创建动态代理并返回
        Subject proxy=(Subject) Proxy.newProxyInstance(handler.getClass().getClassLoader(),subject.getClass().getInterfaces(),handler);
        proxy.hello("hello world.");
    }
}
