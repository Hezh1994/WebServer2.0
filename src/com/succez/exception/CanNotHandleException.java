package com.succez.exception;

/**
 * �������޷�����ʱ���׳����쳣
 * 
 * @author succez
 *
 */
public class CanNotHandleException extends Exception {

	private static final long serialVersionUID = -9147797386713598293L;

	public CanNotHandleException() {
		super();
	}

	public CanNotHandleException(String message) {
		super(message);
	}
}
