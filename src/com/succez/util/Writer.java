package com.succez.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ������д�뵽ͨ���еĹ�����
 * 
 * @author succez
 *
 */
public class Writer {
	private static final Logger LOG = LoggerFactory.getLogger(Writer.class);

	/**
	 * ���ַ���д�뵽ָ����ͨ����
	 * 
	 * @param str
	 *            �ַ���
	 * @param soChannel
	 *            ͨ��
	 * @throws IOException
	 */
	public static void writeToChannel(String str, SocketChannel soChannel) throws IOException {
		byte[] bytes = str.getBytes("utf-8");
		soChannel.write(ByteBuffer.wrap(bytes));
	}

	/**
	 * ���ļ�д�뵽ָ����ͨ����
	 * 
	 * @param file
	 *            File����
	 * @param soChannel
	 *            ͨ��
	 * @throws IOException
	 */
	public static void writeToChannel(File file, SocketChannel soChannel) throws IOException {

		if (!file.exists()) {
			throw new FileNotFoundException("�ļ�������");
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
					LOG.error("�������ر�ʧ��");
				}
			}
		}
	}

	public static void writeHeadToChannel(String key, SocketChannel socketChannel) throws IOException {
		ConfigReader reader = ConfigReader.getConfigReader();
		Map<String, String> map = reader.getMap();
		String value = map.get(key);
		byte[] bytes = value.getBytes("utf-8");
		socketChannel.write(ByteBuffer.wrap(bytes));
	}
}
