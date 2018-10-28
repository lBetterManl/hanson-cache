package com.hanson.web.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * <p>Description: TODO</p>
 * @author Hanson Yan
 * @time   2018年10月26日 下午4:25:24
 */
@Repository("cacheDao")
public class CacheDao {
	private static final Logger log = LoggerFactory.getLogger(CacheDao.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public Map<String, Object> getCache(Integer id) {
		String sql = "select * from tb_user WHERE id = '"+id+"'";
		log.debug("获取信息SQL:{}",sql);
		Map<String, Object> result = jdbcTemplate.queryForMap(sql);
		return result;
	}
	
	public Map<String, Object> updateCache(Integer id, String name) {
		String sql = "UPDATE tb_user SET nickName='"+name+"' WHERE id = '"+id+"'";
		log.debug("更新信息SQL:{}",sql);
		jdbcTemplate.update(sql);
		sql = "select * from tb_user WHERE id = '"+id+"'";
		return jdbcTemplate.queryForMap(sql);
	}
}
