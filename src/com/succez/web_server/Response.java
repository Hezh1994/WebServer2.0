package com.succez.web_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.succez.util.ConfigReader;

/**
 * 响应http请求的应答类，包含应答头和请求的资源。
 * 
 * @author succez
 *
 */
public class Response {
	private String responseHead;
	private byte[] data;

	public Response(String httpHead, byte[] data) {
		super();
		this.responseHead = httpHead;
		this.data = data;
	}

	/**
	 * 返回http应答头的信息
	 * 
	 * @return
	 */
	public String getHttpHead() {
		return responseHead;
	}

	/**
	 * 返回请求的资源
	 * 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * 响应客户端的请求，将Http应答头与客户端请求的资源写入通道中
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public void write(SocketChannel socketChannel) throws IOException {
		ConfigReader reader = ConfigReader.getConfigReader();
		String encoding = reader.getMap().get("encoding");
		byte[] bytes = responseHead.getBytes(encoding);
		// 将http应答头写入通道中
		socketChannel.write(ByteBuffer.wrap(bytes));
		// 将客户端请求的资源写入通道中
		socketChannel.write(ByteBuffer.wrap(data));
	}
}
