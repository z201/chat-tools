package com.github.z201.server.account;

import com.github.z201.common.MsgTools;
import com.github.z201.server.connection.ConnPool;
import com.github.z201.server.connection.TokenFactory;
import com.github.z201.server.connection.TokenPool;
import com.github.z201.common.dto.Account;
import com.github.z201.server.handler.HeartbeatHandler;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 断线重连服务.
 *
 * @author z201.coding@gmail.com.
 */
public class Reconnection {
    private static final Logger logger = LoggerFactory.getLogger(Reconnection.class);

    private Channel channel;
    private String username;
    private Long token;

    public Reconnection(Account account, Channel channel) {
        username = account.getUsername();
        token = account.getToken();
        this.channel = channel;
    }

    /**
     * 登录信息验证
     */
    public void deal() {
        // 验证token
        if (TokenPool.getInstance().query(token)) {
            success();
        } else {
            // token验证失败
            defeat(ProtocolHeader.REQUEST_ERROR);
            logger.info("token验证失败，拒绝重连");
        }
    }

    /**
     * 信息验证成功
     */
    private void success() {
        // 维护连接
        boolean b = ConnPool.add(username, channel);
        // 发送响应数据包
        Account acc = new Account();
        acc.setUsername(username);
        acc.setToken(init());
        Future future = MsgTools.sendReconnectResponse(channel, ProtocolHeader.SUCCESS, Serializer.serialize(acc));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(username + " 重连成功");
                    // 开启心跳检测
                    logger.info(username + " 开启心跳检测");
                    channel.pipeline().addAfter("IdleStateHandler",
                            "HeartbeatHandler", new HeartbeatHandler(channel));
                }
            }
        });
    }

    /**
     * 信息验证失败
     *
     * @param status
     */
    @SuppressWarnings("unchecked")
    private void defeat(byte status) {
        // 发送响应数据包
        Future future = MsgTools.sendReconnectResponse(channel, status, "");
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                logger.info(username + " 重连失败");
                channel.close().sync();
            } else {
                MsgTools.sendReconnectResponse(channel, status, "").addListener((ChannelFutureListener) future11 -> {
                    if (future11.isSuccess()) {
                        logger.info(username + " 重连失败");
                        channel.close().sync();
                    }
                });
            }
        });
    }

    /**
     * 登录信息验证成功后的初始化
     *
     * @return
     */
    private Long init() {
        // 生成token
        TokenFactory factory = new TokenFactory();
        Long token = factory.generate();
        // 维护连接
        ConnPool.add(username, channel);
        // 维护token
        TokenPool.getInstance().add(token);
        return token;
    }


}
