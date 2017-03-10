package service.javassist;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by hanlia on 2017/1/15.
 */
public class Proxy {
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);
    private static final String PROXY_CLASSNAME_PREFIX = "$Proxy";//生成的代理类名称
    private static final AtomicInteger SUFFIX_GENERATOR = new AtomicInteger();//数字后缀

    //代理类访问权限关键字
    private static final boolean SHOULD_BE_FINAL = true;
    private static final boolean SHOULD_BE_ABSTRACT = false;
    private static final boolean SHOULD_BE_PUBLIC = true;
    protected InvocationHandler invocationHandler;

    //构建用于缓存代理类的容器
    private static CompoundKeyWeakHashMap<ClassLoader, Class<?>, Class<?>> proxyClassCache =
            new CompoundKeyWeakHashMap<ClassLoader, Class<?>, Class<?>>();
    protected Proxy(InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    //创建代理对象实例
    public static Object newProxyInstance(ClassLoader classLoader, Class<?> targetClass, InvocationHandler invocationHandler)
            throws Exception {
        classLoader = Objects.requireNonNull(classLoader, "classLoader cannot be null");
        targetClass = Objects.requireNonNull(targetClass, "targetClass cannot be null");
        invocationHandler = Objects.requireNonNull(invocationHandler, "invocationHandler cannot be null");
        Class<?> proxyClass = proxyClassCache.get(classLoader, targetClass);
        if (proxyClass != null) {
            logger.debug("get proxy from cache");
            return proxyClass.getConstructor(InvocationHandler.class).newInstance(invocationHandler);//如果代理类已缓存则创建代理类实例从返回
        }
        ClassPool pool = ClassPool.getDefault();
        String qualifiedName = generateQualifiedName(targetClass);//生成代理类的全限定名
        CtClass proxy = pool.makeClass(qualifiedName);//创建代理类
        setSuperClass(pool, proxy);//设被代理类继承自Proxy
        CtClass[] interfaces = pool.get(targetClass.getName()).getInterfaces();//获取被代理类的所有接口
        int methodIndex = 0;
        for (CtClass parent : interfaces) {
            proxy.addInterface(parent);
            CtMethod[] methods = parent.getDeclaredMethods();// 获取该接口的所有方法
            for (int j = 0; j < methods.length; ++j) {
                CtMethod method = methods[j];
                String fieldSrc = String.format("private static java.lang.reflect.Method method%d = Class.forName(\"%s\").getDeclaredMethods()[%d];"
                        , methodIndex, parent.getName(), j);
                logger.debug("field src for method {}: {}", method.getName(), fieldSrc);
                CtField ctField = CtField.make(fieldSrc, proxy);// 生成字段
                proxy.addField(ctField);// 添加字段
                generateMethod(pool, proxy, method, methodIndex);// 生成对应的Method
                ++methodIndex;
            }
        }
        setModifiers(proxy, SHOULD_BE_PUBLIC, SHOULD_BE_FINAL, SHOULD_BE_ABSTRACT);// 设置代理类的类修饰符
        generateConstructor(pool, proxy);// 生成构造方法
        proxy.writeFile(".");// 持久化class到硬盘
        proxyClass = proxy.toClass(classLoader, null);// 加载持久化到磁盘的class
        proxyClassCache.put(classLoader, targetClass, proxyClass);// 缓存
        return proxyClass.getConstructor(InvocationHandler.class).newInstance(invocationHandler);
    }

    //生成代理类全名
    private static String generateQualifiedName(Class<?> targetClass) throws Exception {
        CtClass theInterface = null;
        for (CtClass parent : ClassPool.getDefault().get(targetClass.getName()).getInterfaces()) {
            if (theInterface == null) {
                theInterface = parent;
            }
            if (!Modifier.isPublic(parent.getModifiers())) {
                theInterface = parent;
                break;
            }
        }
        String name = theInterface.getPackageName() + "." + PROXY_CLASSNAME_PREFIX + SUFFIX_GENERATOR.getAndIncrement();
        return name;
    }

    //设置类修饰符
    private static void setModifiers(CtClass proxy, boolean shouldBePublic, boolean shouldBeFinal, boolean shouldBeAbstract) {
        int modifier = 0;
        modifier = shouldBePublic ? modifier | Modifier.PUBLIC : modifier;
        modifier = shouldBeFinal ? modifier | Modifier.FINAL : modifier;
        modifier = shouldBeAbstract ? modifier | Modifier.ABSTRACT : modifier;
        logger.error(Modifier.toString(modifier));
        proxy.setModifiers(modifier);
    }

    //生成构造函数
    private static void generateConstructor(ClassPool pool, CtClass proxy) throws NotFoundException, CannotCompileException {
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{pool.get(InvocationHandler.class.getName())}, proxy);
        String methodBodySrc = String.format("super(%s);", "$1");
        logger.debug("constructor body for constructor {}: {}", ctConstructor.getName(), methodBodySrc);
        ctConstructor.setBody(methodBodySrc);
        proxy.addConstructor(ctConstructor);
    }


    //生成代理方法
    private static void generateMethod(ClassPool pool, CtClass proxy, CtMethod method, int methodIndex) throws NotFoundException, CannotCompileException {
        CtMethod ctMethod = new CtMethod(method.getReturnType(), method.getName(), method.getParameterTypes(), proxy);
        String methodBodySrc = String.format("return super.invocationHandler.invoke(this, method%d, $args);", methodIndex);
        logger.debug("method body for method {}: {}", method.getName(), methodBodySrc);
        ctMethod.setBody(methodBodySrc);
        proxy.addMethod(ctMethod);
    }

    //把proxy类的父类设置为Proxy
    private static void setSuperClass(ClassPool pool, CtClass proxy) throws CannotCompileException, NotFoundException {
        proxy.setSuperclass(pool.get(Proxy.class.getName()));
    }

}
