package com.succez.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供用于将二进制数据发送到客户端的输出流
 * 
 * @author succez
 *
 */
public class AppOutputStream {
	private static final Logger LOG = LoggerFactory
			.getLogger(AppOutputStream.class);
	private SocketChannel socketChannel;
	private String encoding;

	public AppOutputStream(SocketChannel socketChannel) {
		super();
		this.socketChannel = socketChannel;
		ConfigReader reader = ConfigReader.getConfigReader();
		encoding = reader.getMap().get("encoding");
	}

	public AppOutputStream() {
	}

	/**
	 * 将字符串返回给客户端
	 * 
	 * @param s
	 * @throws IOException
	 *             发生I/O错误
	 */
	public void write(String s) throws IOException {
		byte[] bytes = null;
		try {
			bytes = s.getBytes(encoding);
			socketChannel.write(ByteBuffer.wrap(bytes));
		} catch (UnsupportedEncodingException e) {
			LOG.error("不支持的编码格式");
		}
	}

	/**
	 * 将文件返回给客户端
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void write(File file) throws IOException {
		DataInputStream is = new DataInputStream(new BufferedInputStream(
				new FileInputStream(file)));
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		long fileLength = file.length();
		int len = (int) (fileLength > 4096 ? 4096 : fileLength);
		byte[] bytes = new byte[len];
		int off = 0;
		int numRead;
		try {
			while ((numRead = is.read(bytes, off, len)) != -1) {
				buffer.clear();
				buffer.put(bytes, 0, numRead);
				buffer.flip();
				while (buffer.hasRemaining()) {
					socketChannel.write(buffer);
				}
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("输入流关闭失败");
				}
			}
		}
	}
}
