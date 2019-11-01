package com.taiping.framework.dal.support;

import javax.sql.DataSource;

import com.taiping.framework.dal.client.DalClient;
import com.taiping.framework.dal.dao.DataBaseOperation;

public class DefaultDalClient extends DataBaseOperation implements DalClient {

	public DefaultDalClient(DataSource dataSource) {
		super(dataSource);
	}

}
