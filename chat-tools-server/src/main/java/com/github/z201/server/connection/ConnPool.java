package com.github.z201.server.connection;

import com.github.z201.common.dto.Message;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接池，用户维护已与服务器建立连接的Client
 * <p>
 *
 * @author z201.coding@gmail.com.
 */
public class ConnPool {

    private static final Logger logger = LoggerFactory.getLogger(ConnPool.class);

    private ConnPool() {

    }
    /**
     * 用于存放在线用户的username和channel
     */
    public static Map<String, Channel> onlineMap = new ConcurrentHashMap<>(10);

    /**
     * 添加连接
     *
     * @param username
     * @param channel
     * @return
     */
    public synchronized static boolean add(String username, Channel channel) {
        Channel result = onlineMap.put(username, channel);
        if (result == null) {
            logger.info("Conn池 添加成功(username=" + username + " channel=" + channel + ")");
            return true;
        } else {
            logger.warn("Conn池 添加失败(username=" + username + " channel=" + channel + ")");
            return false;
        }
    }

    /**
     * 删除连接
     *
     * @param username
     * @return
     */
    public synchronized static boolean remove(String username) {
        Channel result = onlineMap.remove(username);
        if (result != null) {
            logger.info("Conn池 移除成功(username=" + username + ")");
            return true;
        } else {
            logger.warn("Conn池 移除失败(username=" + username + ")");
            return false;
        }
    }

    /**
     * 查找连接
     *
     * @param username
     * @return
     */
    public synchronized static Channel query(String username) {
        return onlineMap.get(username);
    }

    /**
     * 查找用户
     *
     * @param channel
     * @return
     */
    public synchronized static String query(Channel channel) {
        Set<Map.Entry<String, Channel>> entries = onlineMap.entrySet();
        Iterator<Map.Entry<String, Channel>> ite = entries.iterator();
        while (ite.hasNext()) {
            Map.Entry<String, Channel> entry = ite.next();
            if (channel.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
