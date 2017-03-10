package service.javassist;

import javassist.Modifier;

public class Tester {
    public void testJavassist() throws Exception {
        Person person = new Person("小明");
        Object proxy = new JavassistProxyFactory(person).getProxy();
        Object proxy1 = new JavassistProxyFactory(person).getProxy();
        ((Talkable) proxy).talk("hello world");//对talk()和smile()的调用被转到InvocationHandler的invoke()
        ((Smileable) proxy).smile();
        System.out.println("package: " + proxy.getClass().getPackage().getName());
        System.out.println("classname: " + proxy.getClass().getName());
        System.out.println("modifiers: " + Modifier.toString(proxy.getClass().getModifiers()));
        System.out.println(proxy.getClass() == proxy1.getClass()); // 测试缓存是否起作用
    }
    public static void main(String[] args){
        try{ new Tester().testJavassist(); }catch (Exception e){}
    }
}