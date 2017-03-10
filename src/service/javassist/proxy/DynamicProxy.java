package service.javassist.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
/**
 * Created by hanlia on 2017/1/16.
 * 使用javassist InvocationHandler
 */
public class DynamicProxy implements InvocationHandler {
    private Object subject;//被代理对象

    public DynamicProxy(Object subject)
    {this.subject=subject;}

    @Override
    public Object invoke(Object object, Method method, Object[] args)throws Throwable{
        System.out.println("invoke before.");
        method.invoke(subject,args);
        //System.out.println(result.toString());
        System.out.println("invoke after.");
        return null;
    }
}
