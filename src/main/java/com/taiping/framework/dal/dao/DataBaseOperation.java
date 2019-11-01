package com.taiping.framework.dal.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.taiping.framework.dal.constant.DbType;
import com.taiping.framework.dal.mapper.RowMapperFactory;
import com.taiping.framework.dal.page.Page;
import com.taiping.framework.dal.parser.FreeMarkerParser;
import com.taiping.framework.dal.parser.SqlBean;
import com.taiping.framework.dal.parser.SqlHolder;
import com.taiping.framework.dal.parser.SqlParser;
import com.taiping.framework.dal.parser.XmlParser;
import com.taiping.framework.dal.util.ParamMapUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 数据操作层，包装了数据层面的基本操作方法
 * 
 * @author
 */
@Slf4j
public abstract class DataBaseOperation {
	
	protected DataSource dataSource;
	
	/** 用Spring提供的jdbcTemplate完成数据库操作的CRUD*/
	protected NamedParameterJdbcTemplate jdbcTemplate;
	
	public DataBaseOperation() {}
	
	public DataBaseOperation(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	protected KeyHolder keyHolder = new GeneratedKeyHolder();
	
	/** SQL失效时间 */
	private static final Long SQLTIMEOUT = 10L;

	/**
	 * 数据持久化
	 * 
	 * @param entity       数据实体
	 * @param requiredType 需要处理的类型
	 * @param <T>          泛型对象
	 * @return 持久化操作成功记录数
	 */
	public <T> Number persist(T entity) {
		SqlHolder genBaseSql = SqlParser.getSqlHolder(entity);
		SqlBean insertSqlBean = genBaseSql.getInsertSqlBean();
		String freSql = insertSqlBean.getFreSql();
		Map<String, Object> paramMap = ParamMapUtil.paramMap(entity);
		logMessage("persist", freSql, paramMap);
		/** 渲染后获取JDBC模板 */
		long beginDate = System.currentTimeMillis();
		jdbcTemplate.update(freSql, new MapSqlParameterSource(paramMap), keyHolder, new String[]{genBaseSql.getEntries()[0].columnName});
		logMessage("persist", freSql, paramMap, System.currentTimeMillis() - beginDate);
		return keyHolder.getKey();
	}
	
	/**
	 * 数据合并与更新
	 * 
	 * @param entity 更新的数据实体
	 * @return 数据更新后的结果
	 */
    public <T> int merge(T entity) {
    	SqlBean updateSqlBean = SqlParser.getSqlHolder(entity).getUpdateSqlBean();
    	Map<String, Object> paramMap = ParamMapUtil.paramMap(entity);
        /** FreeMarker模板渲染 */
    	String freSql = updateSqlBean.getFreSql();
        logMessage("merge", freSql, paramMap);
        long beginDate = System.currentTimeMillis();
        /** 调用JDBCTemplate实现更新，返回更新成功的记录数 */
        int result = jdbcTemplate.update(freSql, paramMap);
        logMessage("merge", freSql, paramMap, System.currentTimeMillis() - beginDate);
        return result;
    }
    
	/**
	 * 数据删除
	 * 
	 * @param entity 删除的数据实体
	 * @return 返回删除的记录数目
	 */
    public <T> int remove(T entity) {
    	SqlBean deleteSqlBean = SqlParser.getSqlHolder(entity).getDeleteSqlBean();
    	Map<String, Object> paramMap = ParamMapUtil.paramMap(entity);
		/** FreeMarker模板渲染 */
    	String freSql = deleteSqlBean.getFreSql();
		logMessage("remove", freSql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现更新，返回更新成功的记录数 */
		int result = jdbcTemplate.update(freSql, paramMap);
		logMessage("remove", freSql, paramMap, System.currentTimeMillis() - beginDate);
		return result;
	}
    
	/**
	 * 根据传入实体类查询单个记录
	 * 
	 * @param entity      查询对象
	 * @param <T>         泛型对象
	 * @return 查询结果
	 */
    public <T> T find(T entity) {
    	SqlBean selectSqlBean = SqlParser.getSqlHolder(entity).getSelectSqlBean();
    	Map<String, Object> paramMap = ParamMapUtil.paramMap(entity);
		/** FreeMarker模板渲染 */
    	String freSql = selectSqlBean.getFreSql();
		logMessage("find", freSql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现单记录查询，并返回查询结果 */
		@SuppressWarnings("unchecked")
		List<T> result = jdbcTemplate.query(freSql, paramMap, new RowMapperFactory<T>((Class<T>) entity.getClass()).getRowMapper());
		logMessage("find", freSql, paramMap, System.currentTimeMillis() - beginDate);
		return singleResult(result);
	}
    
	/**
	 * 根据sqlId查询单条记录
	 * 
	 * @param sqlId     SQLID
	 * @param paramMap  查询参数
	 * @param rowMapper 翻页处理规则
	 * @param <T>       泛型对象
	 * @return 查询结果
	 */
	public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		logMessage("queryForObject", sql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现查询，并返回查询结果 */
		sql = this.limitSql(sql, 1, DbType.ORACLE.name());// 限制结果集规模
		List<T> resultList = jdbcTemplate.query(sql, paramMap, rowMapper);
		logMessage("queryForObject", sql, paramMap, System.currentTimeMillis() - beginDate);
		return singleResult(resultList);
	}
    
	/**
	 * 查询单个记录
	 * 
	 * @param sqlId        SQLID
	 * @param paramMap     查询参数
	 * @param requiredType 需要处理的类型
	 * @param <T>          泛型对象
	 * @return 查询结果
	 */
	public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
		return this.queryForObject(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
	}
	
	/**
	 * queryForObject重载方法
	 * 
	 * @param sqlId        SQLID
	 * @param paramMap     查询参数
	 * @param requiredType 需要处理的类型
	 * @param <T>          泛型对象
	 * @return 查询结果
	 */
	public <T> T queryForObject(String sqlId, Object paramMap, Class<T> requiredType) {
		return this.queryForObject(sqlId, ParamMapUtil.paramMap(paramMap), requiredType);
	}

	/**
	 * 根据sqlId查询多条记录，返回list型结果集
	 * 
	 * @param sqlId     SQLID
	 * @param paramMap  查询参数
	 * @param rowMapper 翻页处理规则
	 * @param <T>       泛型对象
	 * @return 查询结果
	 */
	public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现查询，并返回查询结果 */
		ParamMapUtil.removeMapNull(paramMap);
		logMessage("queryForList(3 paramter)", sql, paramMap);
		List<T> list = jdbcTemplate.query(sql, paramMap, rowMapper);
		logMessage("queryForList(3 paramter)", sql, paramMap, System.currentTimeMillis() - beginDate);
		return list;
	}
	
	/**
	 * queryForList重载方法
	 * 
	 * @param sqlId        SQLID
	 * @param paramMap     查询参数
	 * @param requiredType 需要处理的类型
	 * @param <T>          泛型对象
	 * @return 查询结果
	 */
	public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
		return this.queryForList(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
	}
    
	/**
	 * 查询并返回映射集
	 * 
	 * @param sqlId    SQLID
	 * @param paramMap 查询参数
	 * @return 查询结果
	 */
	public Map<String, Object> queryForMap(String sqlId, Map<String, Object> paramMap) {
		List<Map<String, Object>> queryForList = this.queryForList(sqlId, paramMap);
		return singleResult(queryForList);
	}
	
	/**
	 * 根据sqlId查询多条记录，返回List<Map<String, Object>>型结果集，queryForList重载方法
	 * 
	 * @param sqlId    SQLID
	 * @param paramMap 查询参数
	 * @return 查询结果
	 */
	public List<Map<String, Object>> queryForList(String sqlId, Map<String, Object> paramMap) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		logMessage("queryForMap", sql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现查询，并返回查询结果 */
		sql = this.limitSql(sql, 1, DbType.ORACLE.name());// 限制结果集规模
		List<Map<String, Object>> map = jdbcTemplate.queryForList(sql, paramMap);
		logMessage("queryForMap", sql, paramMap, System.currentTimeMillis() - beginDate);
		return map;
	}
	
	/**
	 * queryForMap重载方法
	 * 
	 * @param sqlId SQLID
	 * @param param 查询参数
	 * @return 查询结果
	 */
	public Map<String, Object> queryForMap(String sqlId, Object param) {
		return this.queryForMap(sqlId, ParamMapUtil.paramMap(param));
	}
	
	/**
	 * 执行查询，返回结果集记录数目
	 * 
	 * @param sqlId    SQLID
	 * @param paramMap 执行参数
	 * @return 查询结果
	 */
	public int execute(String sqlId, Map<String, Object> paramMap) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		logMessage("execute", sql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用JDBCTemplate实现更新，返回更新成功的记录数 */
		ParamMapUtil.removeMapNull(paramMap);
		int result = jdbcTemplate.update(sql, paramMap);
		logMessage("execute", sql, paramMap, System.currentTimeMillis() - beginDate);
		return result;
	}

	/**
	 * 执行查询，execute重载方法
	 * 
	 * @param sqlId SQLID
	 * @param param 执行参数
	 * @return 查询结果
	 */
	public int execute(String sqlId, Object param) {
		return this.execute(sqlId, ParamMapUtil.paramMap(param));
	}
	
	/**
	 * 批量更新
	 * 
	 * @param sqlId       SQLID
	 * @param batchValues 需要批处理的集合
	 * @return 批处理成功记录数
	 */
	public int[] batchUpdate(String sqlId, Map<String, Object>[] batchValues) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		//取第一个map参数生成freemarker替换SQL
		String sql = FreeMarkerParser.processTemplate(orgSql, batchValues[0]);
		logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length));
		long beginDate = System.currentTimeMillis();
		int[] result;
		/** 调用JDBCTemplate批量更新，返回更新成功的记录数 */
		result = jdbcTemplate.batchUpdate(sql, batchValues);
		logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length),
				System.currentTimeMillis() - beginDate);
		return result;
	}
	
	public <T> int[] batchUpdate(String sqlId, @SuppressWarnings("unchecked") T... batchValues) {
		return this.batchUpdate(sqlId, batchValues);
	}
	
	/**
	 * 调存储过程
	 * 
	 * @param sqlId         SQLID
	 * @param paramMap      执行参数
	 * @param sqlParameters sqlcommand参数的对象
	 * @return 存储过程执行结果
	 */
	public Map<String, Object> call(String sqlId, Map<String, Object> paramMap, List<SqlParameter> sqlParameters) {
		ParamMapUtil.removeMapNull(paramMap);
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		logMessage("call", sql, paramMap);
		long beginDate = System.currentTimeMillis();
		/** 调用存储过程 */
		GenericStoredProcedure storedProcedure = new GenericStoredProcedure();
		/** 放入数据源 */
		storedProcedure.setDataSource(dataSource);
		/** 放入SQL */
		storedProcedure.setSql(sql);
		for (SqlParameter sqlParameter : sqlParameters) {
			storedProcedure.declareParameter(sqlParameter);
		}
		Map<String, Object> result = storedProcedure.execute(paramMap);
		logMessage("call", sql, paramMap, System.currentTimeMillis() - beginDate);
		return result;
	}

	/**
	 * 装配分页信息
	 * 
	 * @param template   模板
	 * @param sql        SQL语句
	 * @param paramMap   查询参数
	 * @param pagination 分页
	 * @param dbType     数据源类型
	 */
	public void configurePagination(String sql, Map<String, Object> paramMap, Page page) {
		if (page.getRowCount() == 0 || page.getRowCount() == -1) {
			/** 获取数据总数 */
			int totalRows = jdbcTemplate.queryForObject(sql, paramMap, Integer.class);
			page.setRowCount(totalRows);
		}
	}

	/**
	 * 返回结果集中的第一条记录
	 * 
	 * @param resultList 结果集
	 * @param <T>        泛型对象
	 * @return 结果集中的第一条记录
	 */
	private <T> T singleResult(List<T> resultList) {
		if (resultList != null) {
			int size = resultList.size();
			if (size > 0) {
				if (log.isDebugEnabled() && size > 1) {
					log.debug("Incorrect result size: expected " + 1 + ", actual " + size + " return the first element.");
				}
				return resultList.get(0);
			}
			if (size == 0) {
				if (log.isDebugEnabled()) {
					log.debug("Incorrect result size: expected " + 1 + ", actual " + size);
				}
				return null;
			}
		}
		return null;
	}
	/**
	 * 打印超时sql的执行时间
	 * 
	 * @param method      方法名
	 * @param sql         SQL串
	 * @param object      对象
	 * @param executeTime 执行时间
	 */
	protected void logMessage(String method, String sql, Object object, long executeTime) {
		/** 打印超时sql的执行时间 */
		if (executeTime >= SQLTIMEOUT) {
			if (log.isDebugEnabled()) {
				log.warn(method + " method executeTime:" + executeTime + "ms");
			}
		}
	}

	/**
	 * 打印sql的执行信息
	 * 
	 * @param method 方法名
	 * @param sql    SQL串
	 * @param object 对象
	 */
	protected void logMessage(String method, String sql, Object object) {
		if (log.isDebugEnabled()) {
			log.debug(method + " method SQL: [" + sql + "]");
			log.debug(method + " method parameter:" + object);
		}
	}
	
	private String limitSql(String sql, int size, String dbType) {
		if("mysql".equals(dbType)){
			sql = sql + " LIMIT " +size ;
		}else if("db2".equals(dbType)){
			if( sql.toLowerCase().indexOf(" fetch ") == -1 && sql.toLowerCase().indexOf(" with ")>0 ) {
				sql=sql.substring(0, sql.toLowerCase().indexOf(" with ")) + " fetch first " + size + " rows only " + sql.substring(sql.toLowerCase().indexOf(" with "));			
			} else if( sql.toLowerCase().indexOf(" fetch ") == -1 ) {
				sql=sql + " fetch first " + size + " rows only";
			}
		}else if("oracle".equals(dbType)){
			sql += " AND ROWNUM =" + size;
		}
		return sql;
	}

}
