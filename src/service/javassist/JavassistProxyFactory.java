package service.javassist;

import java.lang.reflect.Method;
public class JavassistProxyFactory implements InvocationHandler{
    private Object target;//被代理类的对象
    public JavassistProxyFactory(Object target) {
        this.target = target;
    }

    //对被代理对象的方法的调用将会转到此方法
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("------- intercept before --------");
        Object result = method.invoke(target, args);//调用传进去的代理对象的方法
        System.out.println("--------intercept after ---------");
        return result;
    }

    // 获取代理类的对象
    public Object getProxy() throws Exception {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), target.getClass(), this);
    }

}