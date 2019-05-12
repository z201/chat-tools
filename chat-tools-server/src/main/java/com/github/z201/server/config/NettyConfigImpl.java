package com.github.z201.server.config;

import com.github.z201.server.handler.AcceptorHandler;
import com.github.z201.common.handler.ProtocolDecoder;
import com.github.z201.common.handler.ProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of NettyConfig.
 *
 * @author z201.coding@gmail.com.
 */
public class NettyConfigImpl implements NettyConfig {

    private static final Logger logger = LoggerFactory.getLogger(NettyConfigImpl.class);
    /**
     * netty 提供的启动服务端端类，对启动做了基本对封装
     */
    private final ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Class channelClass;

    public NettyConfigImpl() {
        bootstrap = new ServerBootstrap();
    }

    @Override
    public void setChannel(Class channelClass) {
        this.channelClass = channelClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setHandler() {
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(channelClass);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
//                pipeline.addLast("LengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 7, 4));
                pipeline.addLast("ProtocolDecoder", new ProtocolDecoder());
                pipeline.addLast("ProtocolEncoder", new ProtocolEncoder());
                pipeline.addLast("IdleStateHandler", new IdleStateHandler(60, 0, 0));
                pipeline.addLast("AcceptorHandler", new AcceptorHandler());
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void bind(int port) {
        bind(port, true);
    }

    @Override
    public void bind(int port, boolean sync) {
        ChannelFuture future = null;
        try {
            future = bootstrap.bind(port).sync();
            logger.info("服务器启动成功 监听端口({})", port);
            if (sync) {
                future.channel().closeFuture().sync();
            } else {
                future.channel().closeFuture();
            }
            logger.info("服务器关闭");
        } catch (InterruptedException e) {
            logger.warn("Netty绑定异常 {}", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public ServerBootstrap getBootstrap() {
        return bootstrap;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }
}
