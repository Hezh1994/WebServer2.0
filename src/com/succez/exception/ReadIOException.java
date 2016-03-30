package com.succez.exception;

/**
 * 当通道可读时，发生I/O错误，则抛出该异常
 * 
 * @author succez
 *
 */
public class ReadIOException extends Exception {
	private static final long serialVersionUID = 1L;

	public ReadIOException() {
		super();
	}

	public ReadIOException(String message) {
		super(message);
	}
}
