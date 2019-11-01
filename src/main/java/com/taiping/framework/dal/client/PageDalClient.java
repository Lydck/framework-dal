package com.taiping.framework.dal.client;

import java.util.Map;

import com.taiping.framework.dal.page.Page;
import com.taiping.framework.dal.page.PageResult;

public interface PageDalClient extends DalClient {

	/**
	 * 获取分页处理结果 重载方法
	 * 
	 * @param sqlId        SQLID
	 * @param paramMap     查询参数
	 * @param requiredType 需要操作的类型
	 * @param page         分页参数
	 * @param <T>          泛型对象
	 * @return List类型的查询结果
	 */
	<T> PageResult<T> queryForList(String sqlId, Map<String, Object> paramMap, Class<T> requiredType, Page page);
}
