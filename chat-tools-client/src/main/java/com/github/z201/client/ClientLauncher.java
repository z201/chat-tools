package com.github.z201.client;

import com.github.z201.common.MsgTools;
import com.github.z201.common.SyncFuture;
import com.github.z201.hander.ClientHandler;
import com.github.z201.hander.ClientHeartbeatHandler;
import com.github.z201.Controller;
import com.github.z201.config.ConfigYaml;
import com.github.z201.core.GuiDict;
import com.github.z201.common.dto.Account;
import com.github.z201.common.handler.ProtocolDecoder;
import com.github.z201.common.handler.ProtocolEncoder;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author z201.coding@gmail.com
 **/
public class ClientLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ClientLauncher.class);

    private Controller controller;

    private ConfigYaml configYaml;

    private Account account;

    public ClientLauncher(Controller controller, ConfigYaml configYaml, Account account) {
        this.controller = controller;
        this.configYaml = configYaml;
        this.account = account;
    }

    private ChannelFuture build(String ip, Integer port) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        // 1.指定线程模型
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("ProtocolDecoder", new ProtocolDecoder());
                        pipeline.addLast("ProtocolEncoder", new ProtocolEncoder());
                        pipeline.addLast("IdleStateHandler", new IdleStateHandler(30, 0, 0));
                        pipeline.addLast("ClientHandler", ClientHandler.getInstance(SyncFuture.getInstance(), controller));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect(ip, port);
        channelFuture.sync().channel();
        return channelFuture;
    }

    public void run() throws RuntimeException {
        try {
            ChannelFuture channelFuture = null;
            channelFuture = this.build(configYaml.getServerUrl(), configYaml.getPort());
            if (channelFuture.isSuccess()) {
                controller.changeStateSelected(true);
            }
            MsgTools.request(channelFuture.channel(), ProtocolHeader.LOGIN, Serializer.serialize(account));
            MessageHolder messageHolder = (MessageHolder) SyncFuture.getInstance().get();
            if (messageHolder.getSign() == ProtocolHeader.RESPONSE) {
                if (messageHolder.getStatus() == ProtocolHeader.SUCCESS) {
                    Account account = Serializer.deserialize(messageHolder.getBody(), Account.class);
                    controller.setAccount(account);
                    controller.setChannel(messageHolder.getChannel());
                    messageHolder.getChannel().pipeline()
                            .addAfter("IdleStateHandler",
                            "ClientHeartbeatHandler",
                                    new ClientHeartbeatHandler(messageHolder.getChannel(), controller, account.getUsername()));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("启动失败");
        }
        controller.getGuiContext().setState(GuiDict.State.启动连接);
    }


}
