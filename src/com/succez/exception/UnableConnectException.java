package com.succez.exception;

/**
 * 服务端通道准备好接受连接时，无法与客户端建立连接，抛出该异常。
 * 
 * @author succez
 *
 */
public class UnableConnectException extends Exception {
	private static final long serialVersionUID = -1314030796006710236L;

	public UnableConnectException() {
		super();
	}

	public UnableConnectException(String message) {
		super(message);
	}
}
