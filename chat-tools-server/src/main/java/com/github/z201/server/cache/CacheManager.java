package com.github.z201.server.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.z201.common.dto.Account;
import com.github.z201.common.dto.Message;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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


    private Cache<String, Message> historicalNews;

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
        user.put("test1", Account.builder().username("test1").password("test").build());
        user.put("test2", Account.builder().username("test2").password("test").build());
    }

    public Account login(String name) {
        return user.getIfPresent(name);
    }

    public void pushMessage(Message message) {
        historicalNews.put(message.getTime().toString(), message);
    }

    public List<Message> pullMessage() {
        ConcurrentMap<String, Message> map = historicalNews.asMap();
        return map.values().stream().collect(Collectors.toList());
    }


}
