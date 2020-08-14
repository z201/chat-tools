package com.github.z201.server.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 维护token
 * <p>
 *
 * @author z201.coding@gmail.com.
 */
public class TokenPool {

    private static final Logger logger = LoggerFactory.getLogger(TokenPool.class);

    /**
     * 枚举类型是线程安全的，并且只会装载一次
     */
    private enum SingletonEnum {
        /**
         * 实例
         */
        INSTANCE;
        /**
         * 声明单例对象
         */
        private final TokenPool instance;

        /**
         * 实例化
         */
        SingletonEnum() {
            instance = new TokenPool();
        }
        private TokenPool getInstance() {
            return instance;
        }
    }
    /**
     * 获取实例（单例对象）
     * @return
     */
    public static TokenPool getInstance() {
        return SingletonEnum.INSTANCE.getInstance();
    }


    private TokenPool() {

    }

    /**
     * 用于存放已生成的token
     */
    private volatile static Set<Long> tokenSet = new HashSet<>();

    /**
     * 添加token
     *
     * @param token
     * @return
     */
    public synchronized boolean add(Long token) {
        if (tokenSet.add(token)) {
            logger.info("Token池 添加成功(token=" + token);
            return true;
        }
        logger.warn("Token池 添加失败(token=" + token);
        return false;
    }

    /**
     * 删除token
     *
     * @param token
     * @return
     */
    public synchronized boolean remove(Long token) {
        if (tokenSet.remove(token)) {
            logger.info("Token池 移除成功(token=" + token);
            return true;
        }
        logger.warn("Token池 移除失败(token=" + token);
        return false;
    }

    /**
     * 查询token是否存在
     *
     * @param token
     * @return
     */
    public synchronized boolean query(Long token) {
        return tokenSet.contains(token);
    }
}
