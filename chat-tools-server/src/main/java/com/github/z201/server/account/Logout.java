package com.github.z201.server.account;

import com.github.z201.common.MsgTools;
import com.github.z201.common.dto.Message;
import com.github.z201.common.dto.OnlineAccount;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import com.github.z201.server.connection.ConnPool;
import com.github.z201.server.connection.TokenPool;
import com.github.z201.common.dto.Account;
import com.github.z201.server.handler.HeartbeatHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 登出服务.
 *
 * @author z201.coding@gmail.com.
 */
public class Logout {
    private static final Logger logger = LoggerFactory.getLogger(Logout.class);

    private Account account;
    private Channel channel;

    public Logout(Account account, Channel channel) {
        this.account = account;
        this.channel = channel;
    }

    public void deal() {
        // 移除维护的连接和token
        TokenPool.getInstance().remove(account.getToken());
        // 标记为登出状态
        HeartbeatHandler.isLogout.set(true);
        Future futureResponse = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(account));
        futureResponse.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Iterator<Map.Entry<String, Channel>> iterator = ConnPool.onlineMap.entrySet().iterator();
                Set<String> nameList = ConnPool.onlineMap.keySet();
                OnlineAccount onlineAccount = OnlineAccount.builder().onlineAccount(nameList).build();
                // 关闭channel
                try {
                    channel.close().sync();
                } catch (InterruptedException e) {
                    logger.warn("关闭channel异常", e);
                }
                Message message = Message.builder()
                        .sender("系统")
                        .content(account.getUsername() + "下线了")
                        .time(System.currentTimeMillis())
                        .build();
                while (iterator.hasNext()) {
                    Channel channel = iterator.next().getValue();
                    List<Message> list = new ArrayList<>();
                    list.add(message);
                    MsgTools.sendMessage(ProtocolHeader.ONLINE_USER_LIST, channel, Serializer.serialize(onlineAccount));
                    MsgTools.sendMessage(ProtocolHeader.ALL_MESSAGE, channel, Serializer.serialize(list));
                }
            }
        });
    }

    private Future sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOGOUT);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
