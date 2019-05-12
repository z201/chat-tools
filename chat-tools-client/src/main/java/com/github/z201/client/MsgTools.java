package com.github.z201.client;

import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;

/**
 * @author z201.coding@gmail.com
 **/
public class MsgTools {

    public static void request(Channel channel, byte type, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.REQUEST);
        messageHolder.setType(type);
        messageHolder.setBody(body);
        channel.writeAndFlush(messageHolder);
    }

}
