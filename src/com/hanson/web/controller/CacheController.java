package com.hanson.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hanson.web.common.GuavaCacheManager;
import com.hanson.web.service.CacheService;

/**
 * <p>Description: TODO</p>
 * @author Hanson Yan
 * @time   2018年10月26日 下午4:21:26
 */

@Controller
@RequestMapping("/cache")
public class CacheController {
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private GuavaCacheManager guavaCacheManager;
	
	@RequestMapping("/test")
	@ResponseBody
	public Map<String, Object> Test(Integer id) {
		Cache cache = guavaCacheManager.getCache("guavaCache");
		Map<String, Object> result = cache.get("entity_"+id, Map.class);
		return result;
	}
	
	@RequestMapping("/get")
	@ResponseBody
	public Map<String, Object> getCache(Integer id) {
		return cacheService.getCache(id);
	}
	
	@RequestMapping("/update")
	@ResponseBody
	public Map<String, Object> updateCache(Integer id, String name) {
		return cacheService.updateCache(id, name);
	}

}
