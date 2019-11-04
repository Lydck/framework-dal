package com.taiping.framework.dal.constant;

import com.taiping.framework.dal.dialect.Dialect;
import com.taiping.framework.dal.dialect.OracleDialect;

/**数据库类型
 * @author xiangyj
 *
 */
public enum DbType {

	MYSQL("MySql", null),
	ORACLE("Oracle", new OracleDialect()),
	DB2("DB2", null);
	
	private String name;
	
	private Dialect dialect;
	
	private DbType(String name, Dialect dialect) {
		this.name = name;
		this.dialect = dialect;
	}

	public Dialect getDialect() {
		return dialect;
	}
	
	private static final DbType[] values = DbType.values();
	
	public static DbType getEnum(String dbType) {
		for(DbType type : values) {
			if(type.name.equals(dbType)) {
				return type;
			}
		}
		return null;
	}
	
}
