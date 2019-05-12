package com.github.z201.server.account;

import com.github.z201.server.connection.TokenPool;
import com.github.z201.common.dto.Account;
import com.github.z201.server.handler.HeartbeatHandler;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        TokenPool.remove(account.getToken());
        // 标记为登出状态
        HeartbeatHandler.isLogout.set(true);
        // 关闭channel
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            logger.warn("关闭channel异常", e);
        }
    }
}
