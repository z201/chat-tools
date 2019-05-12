package com.github.z201.server.config;

/**
 * Netty配置接口.
 *
 * @author z201.coding@gmail.com.
 */
public interface NettyConfig {

    /**
     * 设置需要注册的channel
     *
     * @param channelClass channel类对象
     */
    void setChannel(Class channelClass);

    /**
     * 设置handler
     */
    void setHandler();

    /**
     * 同步绑定端口
     *
     * @param port 绑定端口号
     */
    void bind(int port);

    /**
     * 绑定端口
     *
     * @param port 绑定端口号
     * @param sync 同步->true，异步->false
     */
    void bind(int port, boolean sync);
}
