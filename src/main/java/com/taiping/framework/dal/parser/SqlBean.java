package com.taiping.framework.dal.parser;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SqlBean {

	/** sqlMap中配置的SQL*/
	private String orgSql;
	
	/** 经freemarker动态替换后的SQL*/
	private String freSql;
	
	/** 写入Statement中的SQL*/
	private String stmSql;
	
	public SqlBean() {
		super();
	}
	
	public SqlBean(String freSql, Entry[] entries) {
		super();
		this.freSql = freSql;
	}

	@Override
	public String toString() {
		return "SqlBean [orgSql=" + orgSql + "freSql=" + freSql + " stmSql=" + stmSql + "]";
	}
	
	@Setter
	@Getter
	public static class Entry {
		
		/** Column列名*/
		public String columnName;
		
		/** Oracle递增序列名称*/
		public String sequenceName;
		
		/** 表映射实体对象列对应的字段名称*/
		public String propName;
		
		public Entry(String key, String propName) {
			super();
			this.columnName = key;
			this.propName = propName;
		}
		
		public Entry(String key, String propName, String sequenceName) {
			super();
			this.columnName = key;
			this.propName = propName;
			this.sequenceName = sequenceName;
		}
		
	}
}
