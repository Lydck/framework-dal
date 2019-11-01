package com.taiping.framework.dal.parser;

import com.taiping.framework.dal.parser.SqlBean.Entry;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SqlHolder {

	private SqlBean insertSqlBean;
	
	private SqlBean deleteSqlBean;
	
	private SqlBean updateSqlBean;
	
	private SqlBean selectSqlBean;
	
	private Entry[] entries;

	@Override
	public String toString() {
		return "SqlHolder [insertSqlBean=" + insertSqlBean + ", deleteSqlBean=" + deleteSqlBean + ", updateSqlBean="
				+ updateSqlBean + ", selectSqlBean=" + selectSqlBean + "]";
	}

}
