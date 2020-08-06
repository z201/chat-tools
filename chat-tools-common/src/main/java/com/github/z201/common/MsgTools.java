package com.github.z201.common;

import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author z201.coding@gmail.com
 **/
public class MsgTools {

    /**
     * 请求消息
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


}
