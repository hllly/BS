package service.sreg;

/**
 * Created by hanlia on 2017/1/11.
 * 服务定义接口
 * 首先定义一个基本接口
 */
public interface Service {
    /**
     * 构建服务信息
     * @return
     */
    Object buildInfo();

    /**
     * 完成服务信息的编码并发送给zookeeper注册中心
     * @throws Exception
     */
    void publish(String host,int port,String path) throws Exception;

    /**
     * 启动服务
     */
    void startService(String host,int port);
}
