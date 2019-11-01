package com.taiping.framework.dal.page;

import lombok.Getter;
import lombok.Setter;

/**分页参数
 * @author xiangyj
 *
 */
@Setter
@Getter
public class Page {

	/** 当前页*/
	private int currentPage = 1;
	
	/** 分页大小*/
	private int pageSize = 20;
	
	/** 总页数*/
	private int pageCount;
	
	/** 总记录数*/
	private int rowCount;

	/**获取其实记录位置
	 * @return
	 */
	public int getFirstRowIndex() {
		return (currentPage - 1) * pageSize;
	}
	
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
		if(pageSize <=0 || pageSize > rowCount) {
			pageSize = rowCount;
		}
		pageCount = (rowCount + 1)/ pageSize;
		if(currentPage > pageCount) {
			currentPage = pageCount;
		}
	}
}
