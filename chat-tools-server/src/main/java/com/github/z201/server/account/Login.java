package com.github.z201.server.account;


import com.github.z201.common.MsgTools;
import com.github.z201.common.dto.OnlineAccount;
import com.github.z201.server.cache.CacheManager;
import com.github.z201.server.connection.ConnPool;
import com.github.z201.server.connection.TokenFactory;
import com.github.z201.server.connection.TokenPool;
import com.github.z201.common.dto.Account;
import com.github.z201.common.dto.Message;
import com.github.z201.server.handler.HeartbeatHandler;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * 登录服务.
 * <p>
 *
 * @author z201.coding@gmail.com.
 */
public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    private Channel channel;
    private String username;
    private String password;

    public Login(Account account, Channel channel) {
        username = account.getUsername();
        password = account.getPassword();
        this.channel = channel;
    }

    /**
     * 登录信息验证
     */
    public void deal() {
        CacheManager cacheManager = CacheManager.getInstance();
        Account account = cacheManager.login(username);
        if (account.getPassword().equals(password)) {
            Channel old = ConnPool.query(username);
            if (null != old) {
                logger.info("你被挤下去了");
            }
            success();
        } else {
            defeat(ProtocolHeader.REQUEST_ERROR);
        }
    }

    /**
     * 信息验证成功
     */
    private void success() {
        Long token = init();
        // 发送响应数据包
        Account acc = new Account();
        acc.setUsername(username);
        acc.setToken(token);
        Future future = MsgTools.sendLoginResponse(channel, ProtocolHeader.SUCCESS, Serializer.serialize(acc));
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                logger.info(username + " 登录成功");
                // 开启心跳检测
                logger.info(username + " 开启心跳检测");
                channel.pipeline().addAfter("IdleStateHandler",
                        "HeartbeatHandler", new HeartbeatHandler(channel));
                Iterator<Map.Entry<String, Channel>> iterator = ConnPool.onlineMap.entrySet().iterator();
                Set<String> nameList = ConnPool.onlineMap.keySet();
                OnlineAccount onlineAccount = OnlineAccount.builder().onlineAccount(nameList).build();
                while (iterator.hasNext()) {
                    Channel channel = iterator.next().getValue();
                    List<Message> list = new ArrayList<>();
                    Message message = Message.builder()
                            .sender("系统")
                            .content(username + "上线了")
                            .time(System.currentTimeMillis())
                            .build();
                    list.add(message);
                    MsgTools.sendMessage(ProtocolHeader.ONLINE_USER_LIST, channel, Serializer.serialize(onlineAccount));
                    MsgTools.sendMessage(ProtocolHeader.ALL_MESSAGE, channel, Serializer.serialize(list));
                }
            }
        });
    }

    /**
     * 信息验证失败
     *
     * @param status
     */
    private void defeat(byte status) {
        // 发送响应数据包
        Future future = MsgTools.sendLoginResponse(channel, status, "");
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                logger.info(username + " 登录失败");
                channel.close().sync();
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
