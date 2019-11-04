package com.taiping.framework.dal.dialect;

public interface Dialect {

	/**总行数
	 * @param sql
	 * @return
	 */
	String getRowCountSql(String sql);
	
	/**前几条记录
	 * @param sql
	 * @return
	 */
	String getLimitStringForRandom(String sql);
	
	/**查询从什么位置开始、指定行记录
	 * @param sql
	 * @return
	 */
	String getLimitString(String sql);
	
}
