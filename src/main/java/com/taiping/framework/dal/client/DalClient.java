package com.taiping.framework.dal.client;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;

/**数据库常规操作
 * @author xiangyj
 *
 */
public interface DalClient {

	/**单表添加操作
	 * @param <T>
	 * @param entity 对象实体
	 * @return
	 */
	<T> Number persist(T entity);
	
    /**单表修改操作 根据主键修改记录
     * @param entity
     * @return 更新成功的记录数
     */
	<T> int merge(T entity);
	
	/**单表删除操作 根据主键删除记录
	 * @param <T>
	 * @param entity
	 * @return 删除成功的记录数
	 */
	<T> int remove(T entity);
	
	/**单表查询操作 根据主键查询记录
	 * @param <T>
	 * @param entity
	 * @return
	 */
	<T> T find(T entity);
	
	/**根据sqlId查询单个对象，返回requiredType类型对象，查不到返回null, 查询多个返回第一个
	 * @param <T>
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @param requiredType 结果实体类型
	 * @return
	 */
	<T> T queryForObject(String sqlId, Map<String, Object> paramMap, Class<T> requiredType);
	
	/**根据sqlId查询单个对象，返回requiredType类型对象，查不到返回null, 查询多个返回第一个
	 * @param <T>
	 * @param sqlId SQLID
	 * @param param 查询参数
	 * @param requiredType 结果实体类型
	 * @return
	 */
	<T> T queryForObject(String sqlId, Object param, Class<T> requiredType);
	
	/**根据sqlId查询单个对象，返回rowMapper类型对象, 查不到返回null, 查询多个返回第一个
	 * @param <T>
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @param rowMapper 结果映射
	 * @return
	 */
	<T> T queryForObject(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper);
	
	/**根据sqlId查询单个对象，返回Map集合，key是数据库字段 ，查不到返回null,查询多个返回第一个
	 * @param sqlId
	 * @param paramMap
	 * @return
	 */
	Map<String, Object> queryForMap(String sqlId, Map<String, Object> paramMap);
	
	/**根据sqlId查询单个对象，返回Map集合，key是数据库字段 ，查不到返回null,查询多个返回第一个
	 * @param sqlId
	 * @param param
	 * @return
	 */
	Map<String, Object> queryForMap(String sqlId, Object param);
	
	/**根据sqlId查询多个对象，返回requiredType类型对象List集合
	 * @param <T>
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @param requiredType 结果实体类型
	 * @return
	 */
	<T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, Class<T> requiredType);
	
	/**根据sqlId查询，返回Map集合List，key是数据库字段
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @return
	 */
	List<Map<String, Object>> queryForList(String sqlId, Map<String, Object> paramMap);
	
	/**根据sqlId查询多个对象，返回rowMapper类型对象List集合
	 * @param <T>
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @param rowMapper 结果映射
	 * @return
	 */
	<T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper);
	
	/**根据sqlId执行，返回执行成功的记录条数
	 * @param sqlId SQLID
	 * @param paramMap 查询参数
	 * @return
	 */
	int execute(String sqlId, Map<String, Object> paramMap);
	
    /**根据sqlId执行，返回执行成功的记录条数
     * @param sqlId SQLID
     * @param param 参数对象
     * @return
     */
    int execute(String sqlId, Object param);
    
    /**根据sqlId执行，批量执行
     * @param sqlId SQLID
     * @param batchValues 批处理对象
     * @return 执行成功的记录数
     */
    int[] batchUpdate(String sqlId, Map<String, Object>[] batchValues);
    
    /**根据sqlId执行，批量执行
     * @param <T>
     * @param sqlId SQLID
     * @param batchValues 批处理对象
     * @return 执行成功的记录数
     */
    <T> int[] batchUpdate(String sqlId, @SuppressWarnings("unchecked") T... batchValues);
    
	/**
	 * 存储过程调用 存储过程调用时，需要加上schema
	 * 
	 * @param sqlId         SQL语句ID
	 * @param paramMap      查询参数
	 * @param sqlParameters SqlCommand参数
	 * @return 调用结果
	 */
	Map<String, Object> call(String sqlId, Map<String, Object> paramMap, List<SqlParameter> sqlParameters);
}
