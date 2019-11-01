package com.taiping.framework.dal.exception;

public class DalException extends RuntimeException {
	
	private String msg;

	private static final long serialVersionUID = 1L;

	public DalException(String msg) {
		super(msg);
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "DalException [msg=" + msg + "]";
	}
	
}
