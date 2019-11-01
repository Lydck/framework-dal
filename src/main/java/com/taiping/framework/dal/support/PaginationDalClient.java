package com.taiping.framework.dal.support;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.taiping.framework.dal.client.PageDalClient;
import com.taiping.framework.dal.mapper.RowMapperFactory;
import com.taiping.framework.dal.page.Page;
import com.taiping.framework.dal.page.PageResult;
import com.taiping.framework.dal.parser.FreeMarkerParser;
import com.taiping.framework.dal.parser.XmlParser;

public class PaginationDalClient extends DefaultDalClient implements PageDalClient {
	
	/** 结果集大小*/
	private static final String LIMIT = "_limit";
	/** 偏移量，起始位置*/
	private static final String OFFSET = "_offset";

	public PaginationDalClient(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public <T> PageResult<T> queryForList(String sqlId, Map<String, Object> paramMap, Class<T> requiredType, Page page) {
		String orgSql = XmlParser.getOrgSql(sqlId);
		/** FreeMarker模板渲染 */
		String sql = FreeMarkerParser.processTemplate(orgSql, paramMap);
		List<T> list = null;
		if (page.getPageSize() < 0) {
			page.setPageSize(1000);
			page.setCurrentPage(1);
        }
		paramMap.put(LIMIT, page.getPageSize());
        paramMap.put(OFFSET, page.getFirstRowIndex());
        logMessage("queryForList", sql, paramMap);
        long beginDate = System.currentTimeMillis();
        /** 获取数据总数 */
		this.configurePagination(OracleDialect.getRowCountSql(sql), paramMap, page);
        /** 执行分页查询 */
        list = jdbcTemplate.query(OracleDialect.getLimitString(sql), paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
        logMessage("queryForList", sql, paramMap, System.currentTimeMillis() - beginDate);
        return new PageResult<T>(list, page);
	}

}
