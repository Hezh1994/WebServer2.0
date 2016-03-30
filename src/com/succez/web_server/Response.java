package com.succez.web_server;

import java.nio.channels.SocketChannel;

import com.succez.util.AppOutputStream;

/**
 * 响应http请求的应答类，包含应答头和请求的资源。
 * 
 * @author succez
 *
 */
public class Response {
	private AppOutputStream outputStream;

	public Response(SocketChannel socketChannel) {
		this.outputStream = new AppOutputStream(socketChannel);
	}

	/**
	 * 返回将数据发送给客户端的输出流
	 * 
	 * @return
	 */
	public AppOutputStream getOutputStream() {
		return outputStream;
	}
}
