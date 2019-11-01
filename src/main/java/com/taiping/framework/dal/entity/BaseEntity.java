package com.taiping.framework.dal.entity;

public abstract class BaseEntity {

	/** 分库路由*/
	private int routeId;

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	
}
