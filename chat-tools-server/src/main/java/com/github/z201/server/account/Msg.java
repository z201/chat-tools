package com.github.z201.server.account;

import com.github.z201.common.MsgTools;
import com.github.z201.common.dto.Message;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.ProtocolHeader;
import com.github.z201.server.cache.CacheManager;
import com.github.z201.server.connection.ConnPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


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
        /**
         * 1、先给自己发消息，发送成功则给其他人发送消息，
         */
        List<Message> list = new ArrayList<>();
        list.add(msg);
        Future future = MsgTools.sendMessage(ProtocolHeader.ALL_MESSAGE, channel, Serializer.serialize(list));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(msg.getSender() + " 消息处理成功");
                    /**
                     * 获取其他在线的连接
                     */
                    Iterator<Map.Entry<String, Channel>> iterator = ConnPool.onlineMap.entrySet().iterator();
                    List<Message> list = new ArrayList<>();
                    list.add(msg);
                    while (iterator.hasNext()) {
                        String username = iterator.next().getKey();
                        /**
                         * 给其他人推送消息
                         */
                        if (!msg.getSender().equals(username)) {
                            Channel channel = ConnPool.query(username);
                            MsgTools.sendMessage(ProtocolHeader.ALL_MESSAGE, channel, Serializer.serialize(list));
                        }
                    }
                }
            }
        });
    }

}
