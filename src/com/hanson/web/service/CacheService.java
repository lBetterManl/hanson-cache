package com.hanson.web.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hanson.web.dao.CacheDao;

/**
 * <p>Description: TODO</p>
 * @author Hanson Yan
 * @time   2018年10月26日 下午4:24:42
 */
@Service("cacheService")
public class CacheService {
	
	@Autowired
	private CacheDao cacheDao;
	
	@Cacheable(value = "guavaCache",key="'entity_'+#id")
	public Map<String, Object> getCache(Integer id) {
		return cacheDao.getCache(id);
	}
	
	@CachePut(value = "guavaCache", key="'entity_'+#id")
	public Map<String, Object> updateCache(Integer id, String name) {
		return cacheDao.updateCache(id, name);
	}

}
