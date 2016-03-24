package com.succez.web_server;

import java.io.InputStream;

/**
 * 响应http请求的应答类，包含应答头和请求的资源。
 * 
 * @author succez
 *
 */
public class Response {
	private byte[] responseHead;
	private long fileLength;
	private InputStream resource;

	public Response(byte[] responseHead, long fileLength, InputStream resource) {
		super();
		this.responseHead = responseHead;
		this.fileLength = fileLength;
		this.resource = resource;
	}

	/**
	 * 获取资源大小
	 * 
	 * @return
	 */
	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	/**
	 * 返回http应答头的信息
	 * 
	 * @return
	 */
	public byte[] getHttpHead() {
		return responseHead;
	}

	/**
	 * 返回请求的资源
	 * 
	 * @return
	 */
	public InputStream getData() {
		return resource;
	}

	/**
	 * 响应客户端的请求，将Http应答头与客户端请求的资源写入通道中
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
}
