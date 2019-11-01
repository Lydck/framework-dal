package com.taiping.framework.dal.support;

/**
 * SQL分页封装，Oracle方言<br>
 * 〈功能详细描述〉
 * 
 * @author
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class OracleDialect {
	
	public static String getRowCountSql(String sql) {
		return new StringBuffer("select count(1) from (").append(sql).append(")").toString();
	}
	
	/**
	 * 封装SQL，查询前几条记录
	 * 
	 * @param sql --源SQL
	 * @return SQL串
	 */
	public String getLimitStringForRandom(String sql) {
		return new StringBuffer(sql.length() + 100).append("select t.*,rownum rn from (").append(sql)
				.append(") t where ROWNUM <= :_limit)").toString();
	}

	/**
	 * 封装SQL，查询从什么位置开始、指定行记录
	 * 
	 * @param sql --源SQL
	 * @return SQL串
	 */
	public static String getLimitString(String sql) {
		return new StringBuffer(sql.length() + 100).append("select * from (select t.*, ROWNUM rn from (").append(sql)
				.append(") t where ROWNUM <= (:_offset + :_limit)) tb where rn > :_offset").toString();
	}
	
	public static void main(String[] args) {
		System.out.println(getRowCountSql("select * from tms_user"));
	}
}
