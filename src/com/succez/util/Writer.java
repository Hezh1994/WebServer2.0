package com.succez.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将数据写入到通道中的工具类
 * 
 * @author succez
 *
 */
public class Writer {
	private static final Logger LOG = LoggerFactory.getLogger(Writer.class);

	/**
	 * 将字符串写入到指定的通道中
	 * 
	 * @param str
	 *            字符串
	 * @param soChannel
	 *            通道
	 * @throws IOException
	 */
	public static void writeToChannel(String str, SocketChannel soChannel)
			throws IOException {
		byte[] bytes = str.getBytes("utf-8");
		soChannel.write(ByteBuffer.wrap(bytes));
	}

	/**
	 * 将文件写入到指定的通道中
	 * 
	 * @param file
	 *            File对象
	 * @param soChannel
	 *            通道
	 * @throws IOException
	 */
	public static void writeToChannel(File file, SocketChannel soChannel)
			throws IOException {

		if (!file.exists()) {
			throw new FileNotFoundException("文件不存在");
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			while (is.read(bytes) != -1) {
				soChannel.write(ByteBuffer.wrap(bytes));
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

	public static void writeHeadToChannel(String key,
			SocketChannel socketChannel) throws IOException {
		Properties properties = new Properties();
		InputStream is = Writer.class
				.getResourceAsStream("/httpResponse.properties");
		properties.load(is);
		is.close();
		String value = properties.getProperty(key);
		byte[] bytes = value.getBytes("utf-8");
		socketChannel.write(ByteBuffer.wrap(bytes));
	}
}
