package com.github.z201.server.service;


import com.github.z201.server.account.*;
import com.github.z201.common.dto.Account;
import com.github.z201.common.dto.Message;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import com.github.z201.server.connection.ConnPool;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;


/**
 * 业务分发器.
 *
 * @author z201.coding@gmail.com.
 */
public class Dispatcher {

    public static void dispatch(MessageHolder messageHolder) {

        if (messageHolder.getSign() != ProtocolHeader.REQUEST) {
            // 请求错误
            response(messageHolder.getChannel(), messageHolder.getSign());
            return;
        }

        switch (messageHolder.getType()) {
            // 登录
            case ProtocolHeader.LOGIN:
                Account aLogin = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Login(aLogin, messageHolder.getChannel()).deal();
                break;

            // 登出
            case ProtocolHeader.LOGOUT:
                Account aLogout = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Logout(aLogout, messageHolder.getChannel()).deal();
                break;

            // 消息
            case ProtocolHeader.ALL_MESSAGE:
                Message message = Serializer.deserialize(messageHolder.getBody(), Message.class);
                new Msg(message,messageHolder.getChannel()).deal();
                break;

            // 断线重连
            case ProtocolHeader.RECONN:
                Account account = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Reconnection(account, messageHolder.getChannel()).deal();
                break;

            // 请求错误
            default:
                response(messageHolder.getChannel(), messageHolder.getSign());
                break;
        }

        // 释放buffer
        ReferenceCountUtil.release(messageHolder);
    }

    /**
     * 请求错误响应
     *
     * @param channel
     * @param sign
     */
    private static void response(Channel channel, byte sign) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(sign);
        messageHolder.setStatus(ProtocolHeader.REQUEST_ERROR);
        messageHolder.setBody("");
        channel.writeAndFlush(messageHolder);
    }
}
