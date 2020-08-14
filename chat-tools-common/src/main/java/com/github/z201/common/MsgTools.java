package com.github.z201.common;

import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;

/**
 * @author z201.coding@gmail.com
 **/
public class MsgTools {

    /**
     * 请求消息
     *
     * @param channel
     * @param type
     * @param body
     * @return
     */
    public static ChannelFuture request(Channel channel, byte type, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.REQUEST);
        messageHolder.setType(type);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }

    /**
     * 响应
     *
     * @param channel
     * @param type
     */
    public static void response(Channel channel, byte type) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(type);
        messageHolder.setStatus(ProtocolHeader.SUCCESS);
        messageHolder.setBody("");
        channel.writeAndFlush(messageHolder);
    }

    /**
     * 服务器繁忙响应
     *
     * @param channel
     * @param sign
     */
    public static void busyResponse(Channel channel, byte sign) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(sign);
        messageHolder.setStatus(ProtocolHeader.SERVER_BUSY);
        messageHolder.setBody("");
        channel.writeAndFlush(messageHolder);
    }

    /**
     * 响应登录请求
     * @param channel
     * @param status
     * @param body
     * @return
     */
    public static Future sendLoginResponse(Channel channel, byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOGIN);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }

    /**
     * 发送消息
     *
     * @param type
     * @param recChannel
     * @param body
     * @return
     */
    public static ChannelFuture sendMessage(byte type, Channel recChannel, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.NOTICE);
        messageHolder.setType(type);
        messageHolder.setStatus(ProtocolHeader.SUCCESS);
        messageHolder.setBody(body);
        return recChannel.writeAndFlush(messageHolder);
    }


    /**
     * 请求错误响应
     *
     * @param channel
     * @param sign
     */
    public static void errorResponse(Channel channel, byte sign) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(sign);
        messageHolder.setStatus(ProtocolHeader.REQUEST_ERROR);
        messageHolder.setBody("");
        channel.writeAndFlush(messageHolder);
    }

    /**
     * 响应重连
     *
     * @param channel
     * @param status
     * @param body
     * @return
     */
    public static Future sendReconnectResponse(Channel channel, byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.RECONNCET);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }


}
