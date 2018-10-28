/**
 * Project: hanson-cache
 * File: GuavaCacheManager.java
 * Package: com.hanson.web.common
 * Date: 2018年10月26日 下午7:28:14
 * Copyright (c) 2018, hanson.yan@qq.com All Rights Reserved.
 */
package com.hanson.web.common;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

/**
 * <p>Description: 对guava cache管理</p>
 * @author Hanson
 * @since  JDK 1.8
 * @time 2018年10月26日 下午7:28:14
 * guavacache支持事务回滚，重写manager。
 * cache的构建使用builder配置构建
 */
public class GuavaCacheManager extends AbstractTransactionSupportingCacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = Maps.newConcurrentMap();

    private boolean dynamic = true;

    private Map<String, CacheBuilder> builderMap = Maps.newHashMap();

    private boolean allowNullValues = true;

    @Override
    protected Collection<? extends Cache> loadCaches() {
        Collection<Cache> values = cacheMap.values();
        return values;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = this.cacheMap.get(name);
        if (cache == null && this.dynamic) {
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null && this.builderMap.containsKey(name)) {
                    CacheBuilder builder = this.builderMap.get(name);
                    cache = createGuavaCache(name, builder);
                    this.cacheMap.put(name, cache);
                }
            }
        }
        return cache;
    }

    protected Cache createGuavaCache(String name, CacheBuilder builder) {
        com.google.common.cache.Cache<Object, Object> cache = null;
        if(builder == null){
            cache = CacheBuilder.newBuilder().build();
        }else{
            cache = builder.build();
        }
        return new GuavaCache(name, cache, isAllowNullValues());
    }


    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    public void setConfigMap(Map<String, CacheBuilder> configMap) {
        this.builderMap = configMap;
    }

}
