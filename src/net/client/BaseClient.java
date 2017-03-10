package net.client;

import io.netty.bootstrap.Bootstrap;

/**
 * Created by hanlianlian on 2017/1/3.
 * 客户端引导类基本接口
 * 定义connect()方法
 * 该方法用于向服务端发起连接
 * 用户自定义客户端引导类与CommonClient类均继承此接口
 */
public interface BaseClient {
    void connect(Bootstrap bootstrap);
}
