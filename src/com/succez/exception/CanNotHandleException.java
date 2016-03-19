package com.succez.exception;

/**
 * 当请求无法处理时，抛出该异常
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
