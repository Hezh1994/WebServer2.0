package com.succez.web_server;

import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.util.AppOutputStream;

/**
 * 响应http请求的应答类，包含应答头和请求的资源。
 * 
 * @author succez
 *
 */
public class Response {
	private static final Logger LOG = LoggerFactory.getLogger(Response.class);
	private AppOutputStream outputStream;

	public Response(SocketChannel socketChannel) {
		LOG.info("响应客户端请求");
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
