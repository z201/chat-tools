package com.github.z201.hander;

import com.github.z201.common.MsgTools;
import com.github.z201.Controller;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author z201.coding@gmail.com
 **/
public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHeartbeatHandler.class);

    public static AtomicBoolean isLogout = new AtomicBoolean(false);

    private Channel channel;
    private Controller controller;
    private String username;

    public ClientHeartbeatHandler(Channel channel, Controller controller, String username) {
        this.channel = channel;
        this.controller = controller;
        this.username = username;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (logger.isDebugEnabled()) {
                logger.debug(username + " 发送服务器心跳包");
            }
            MsgTools.request(channel, ProtocolHeader.HEARTBEAT, "");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (isLogout.get()) {
            isLogout.set(false);
            if (logger.isDebugEnabled()) {
                logger.debug(username + " 退出登录");
            }
            controller.console(username + " 退出登录");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(username + " 与服务器断开连接");
            }
            controller.console(username + " 与服务器断开连接");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageHolder) {
            MessageHolder messageHolder = (MessageHolder) msg;
            if (messageHolder.getType() == ProtocolHeader.HEARTBEAT) {
                if (logger.isDebugEnabled()) {
                    logger.debug(username + " 收到服务器心跳包");
                }
                ReferenceCountUtil.release(msg);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }
}
