package com.succez.exception;

/**
 * 当通道可写时，在像通道写入数据时发生错误，则抛出该异常。
 * 
 * @author succez
 *
 */
public class WriteIOException extends Exception {
	private static final long serialVersionUID = -6558639211223334071L;

	public WriteIOException() {
		super();
	}

	public WriteIOException(String message) {
		super(message);
	}
}
