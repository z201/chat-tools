package com.github.z201.server.account;

import com.github.z201.common.dto.Message;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import com.github.z201.server.cache.CacheManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author z201.coding@gmail.com
 **/
public class Msg {

    private static final Logger logger = LoggerFactory.getLogger(Logout.class);

    private Message msg;
    private Channel channel;

    public Msg(Message msg, Channel channel) {
        this.msg = msg;
        this.channel = channel;
    }

    public void deal() {
        CacheManager cacheManager = CacheManager.getInstance();
        msg.setTime(System.currentTimeMillis());
        cacheManager.pushMessage(msg);
        success(msg, ProtocolHeader.SUCCESS);
    }

    private void success(Message msg, byte status) {
        Future future = response(status, Serializer.serialize(msg));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(msg.getSender() + " 消息处理成功");
                }
            }
        });
    }

    private Future response(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.ALL_MESSAGE);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
