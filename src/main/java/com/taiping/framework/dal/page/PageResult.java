package com.taiping.framework.dal.page;

import java.util.List;

public class PageResult<T> {

	private List<T> pageR;

	public PageResult(List<T> list, Page page) {
		this.pageR = list;
	}

	public List<T> getPageR() {
		return pageR;
	}

	public void setPageR(List<T> pageR) {
		this.pageR = pageR;
	}

	@Override
	public String toString() {
		return "PageResult [pageR=" + pageR + "]";
	}
	
}
