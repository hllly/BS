package service.javassist.proxy;

/**
 * Created by hanlia on 2017/1/16.
 * 真实的被代理对象
 */
public class RealSubject implements Subject{
    @Override
    public void rent(){System.out.println("realSubject rent.");}
    @Override
    public String hello(String str){return str;}
}
