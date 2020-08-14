package com.github.z201.server.handler;

import com.github.z201.common.MsgTools;
import com.github.z201.common.dto.Message;
import com.github.z201.common.dto.OnlineAccount;
import com.github.z201.common.json.Serializer;
import com.github.z201.server.connection.ConnPool;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 心跳检测Handler
 * <p>
 *
 * @author z201.coding@mail.com.
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    public static AtomicBoolean isLogout = new AtomicBoolean(false);

    private Channel channel;
    private String username;

    /**
     * 丢失的心跳数
     */
    private int counter = 0;

    public HeartbeatHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (username == null) {
                username = ConnPool.query(channel);
            }
            // 心跳丢失
            counter++;
            logger.info(username + " 丢失" + counter + "个心跳包");
            if (counter > 4) {
                // 心跳丢失数达到5个，主动断开连接
                ctx.channel().close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String name = ConnPool.query(ctx.channel());
        ConnPool.remove(name);
        if (isLogout.get()) {
            isLogout.set(false);
            logger.info(name + " 退出登录");
        } else {
            Iterator<Map.Entry<String, Channel>> iterator = ConnPool.onlineMap.entrySet().iterator();
            Set<String> nameList = ConnPool.onlineMap.keySet();
            OnlineAccount onlineAccount = OnlineAccount.builder().onlineAccount(nameList).build();
            Message message = Message.builder()
                    .sender("系统")
                    .content(name + "下线了")
                    .time(System.currentTimeMillis())
                    .build();
            List<Message> list = new ArrayList<>();
            list.add(message);
            while (iterator.hasNext()) {
                Channel channel = iterator.next().getValue();
                MsgTools.sendMessage(ProtocolHeader.ONLINE_USER_LIST, channel, Serializer.serialize(onlineAccount));
                MsgTools.sendMessage(ProtocolHeader.ALL_MESSAGE, channel, Serializer.serialize(list));
            }
            logger.info(name + " 与服务器断开连接");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageHolder) {
            MessageHolder messageHolder = (MessageHolder) msg;
            if (messageHolder.getType() == ProtocolHeader.HEARTBEAT) {
                if (username == null) {
                    username = ConnPool.query(channel);
                }
                logger.info(username + " 收到心跳包");
                // 心跳丢失清零
                counter = 0;
                MsgTools.response(channel, ProtocolHeader.HEARTBEAT);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

}