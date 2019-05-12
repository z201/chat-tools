package com.github.z201.server.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.z201.common.dto.Account;
import com.github.z201.common.dto.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author z201.coding@gmail.com
 **/
public class CacheManager {

    private static volatile CacheManager single = null;

    private CacheManager() {

    }

    public static CacheManager getInstance() {
        if (null == single) {
            synchronized (CacheManager.class) {
                if (null == single) {
                    single = new CacheManager();
                    single.init();
                }
            }
        }
        return single;
    }

    public static final String H_MESSAGE = "H_MESSAGE";

    private Cache<String, List<Message>> historicalNews;

    private Cache<String, Account> user;

    private void init() {
        historicalNews = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .maximumSize(10)
                .build();
        user = Caffeine.newBuilder()
                .maximumSize(10)
                .build();
        user.put("test", Account.builder().username("test").password("test").build());
        user.put("king", Account.builder().username("king").password("king").build());
    }

    public Account login(String name) {
        return user.getIfPresent(name);
    }

    public void pushMessage(Message message) {
        List<Message> messages = historicalNews.get(H_MESSAGE, k -> initMessage());
        messages.add(message);
        historicalNews.put(H_MESSAGE, messages);
    }

    public List<Message> pullMessage() {
        return historicalNews.get(H_MESSAGE, k -> initMessage());
    }

    private List<Message> initMessage() {
        return new ArrayList<>();
    }


}
