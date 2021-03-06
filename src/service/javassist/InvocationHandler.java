package service.javassist;

import java.lang.reflect.Method;

/**
 * @author lixiaohui
 * @date 2016年9月27日 下午9:53:59
 *
 */
public interface InvocationHandler {

    /**
     * 业务逻辑填充
     *
     * @param proxy 生成的代理对象
     * @param method 调用的方法
     * @param args 调用该方法的参数
     * @return 调用该方法的返回值
     * @throws Throwable throws if any exception
     */
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

}