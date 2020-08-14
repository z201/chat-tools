package com.github.z201.server;

import com.github.z201.server.config.NettyConfig;
import com.github.z201.server.config.NettyConfigImpl;
import com.github.z201.server.service.Service;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 启动类
 * @author z201.coding@gmail.com
 */
public class Launcher {
    public static void main(String[] args) {
        new Service().initAndStart();
        NettyConfig config = new NettyConfigImpl();
        config.setChannel(NioServerSocketChannel.class);
        config.setHandler();
        config.bind(20000);
    }

}
