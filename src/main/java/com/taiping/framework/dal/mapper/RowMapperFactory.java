package com.taiping.framework.dal.mapper;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * 
 * 映射类型工厂
 * 
 * @param <T> class 实现类
 * @author
 */
public class RowMapperFactory<T> {
	/**
	 * 需处理的类型
	 */
	private Class<T> requiredType;

	/**
	 * 构造函数
	 * 
	 * @param requiredType 需处理的类型
	 */
	public RowMapperFactory(Class<T> requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * 获取翻页处理逻辑
	 * 
	 * @return 翻页处理逻辑
	 */
	public RowMapper<T> getRowMapper() {
		if (requiredType.equals(String.class) || Number.class.isAssignableFrom(requiredType) || requiredType.equals(Date.class)) {
			return new SingleColumnRowMapper<T>(requiredType);
		} else {
			return new DefaultBeanPropertyRowMapper<T>(requiredType);
		}
	}
}
