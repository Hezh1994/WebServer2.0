package com.succez.handle;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.appliction.Appliction;
import com.succez.exception.CanNotHandleException;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

public class ResponseThread extends Thread {
	private static final Logger LOG = LoggerFactory
			.getLogger(ResponseThread.class);
	private Appliction app;
	private SocketChannel socketChannel;
	private Request request;
	private Response response;

	public ResponseThread(Appliction app, Request request, Response response,
			SocketChannel socketChannel) {
		super();
		this.app = app;
		this.request = request;
		this.response = response;
		this.socketChannel = socketChannel;
	}

	public void run() {
		try {
			app.service(request, response);
		} catch (CanNotHandleException e) {
			LOG.error("无法处理的请求类型，只支持处理GET请求");
		} catch (IOException e) {
			LOG.error("无法响应客户端请求");
		} finally {
			try {
				socketChannel.close();
			} catch (IOException e) {
				LOG.error("客户端通道关闭失败");
			}
		}
	}
}
