package service.javassist;

/**
 * Created by hanlia on 2017/1/16.
 */
public class Cache<K,V,M> {
    private K classLoader;
    private V targetClass;
    private M proxyClass;

    public K getClassLoader(){return classLoader;}
    public V getTargetClass(){ return targetClass; }
    public M getProxyClass(){ return proxyClass; }

    public void setClassLoader(K classLoader){this.classLoader=classLoader;}
    public void setTargetClass(V targetClass){this.targetClass=targetClass;}
    public void setProxyClass(M proxyClass){this.proxyClass=proxyClass;}
}
