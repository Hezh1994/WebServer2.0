package com.succez.web_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.succez.util.ConfigReader;

/**
 * ��Ӧhttp�����Ӧ���࣬����Ӧ��ͷ���������Դ��
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
	 * ����HttpӦ��ͷ����Ϣ
	 * 
	 * @return
	 */
	public String getHttpHead() {
		return responseHead;
	}

	/**
	 * �����������Դ
	 * 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * ��Ӧ�ͻ��˵����󣬽�HttpӦ��ͷ�뷵�ظ��ͻ��˵���Դд��ͨ���С�
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public void write(SocketChannel socketChannel) throws IOException {
		ConfigReader reader = ConfigReader.getConfigReader();
		String encoding = reader.getMap().get("encoding");
		byte[] bytes = responseHead.getBytes(encoding);
		// ��httpӦ��ͷд��ͨ����
		socketChannel.write(ByteBuffer.wrap(bytes));
		// ���ͻ����������Դд��ͨ����
		socketChannel.write(ByteBuffer.wrap(data));
	}
}
