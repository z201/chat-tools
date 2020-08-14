package com.github.z201.server.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 用户创建token
 * <p>
 * @author z201.coding@gmail.com.
 */
public class TokenFactory {
    private static final Logger logger = LoggerFactory.getLogger(TokenFactory.class);

    public TokenFactory() {
    }

    /**
     * 调用此方法生成一个token
     *
     * @return
     */
    public Long generate() {
        Long token;

        Random random = new Random();

        while (isExist(token = random.nextLong())) {
            token = random.nextLong();
        }
        logger.info("创建Token(" + token + ")");

        return token;
    }

    private boolean isExist(Long token) {
        return TokenPool.getInstance().query(token);
    }
}
