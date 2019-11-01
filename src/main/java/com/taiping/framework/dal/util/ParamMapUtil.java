package com.taiping.framework.dal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanMap;

public class ParamMapUtil {

	private static final Map<Class<?>, BeanMap> beanMaps = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> paramMap(T entity) {
		if (entity instanceof Map) {
            return (Map<String, Object>) entity;
        }
		BeanMap beanMap = null;
		Class<? extends Object> tClass = entity.getClass();
		if(beanMaps.containsKey(tClass)) {
			beanMap = beanMaps.get(tClass);
		} else {
			beanMap = BeanMap.create(entity);
			beanMaps.putIfAbsent(tClass, beanMap);
		}
		beanMap.setBean(entity);
		Map<String, Object> result = new HashMap<>();
		Set<String> keySet = beanMap.keySet();
		for(String key : keySet) {
			result.put(key, beanMap.get(key));
		}
		return result;
	}
	
	public static <T> BeanMap getBeanMap(T entity) {
		paramMap(entity);
		return beanMaps.get(entity.getClass());
	}
	
	/**删除paramMap中value为空的元素
	 * @param paramMap
	 */
	public static void removeMapNull(Map<String, Object> paramMap) {
		paramMap.values().removeIf(value -> value == null || value.equals(""));
	}
}
