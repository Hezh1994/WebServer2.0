package com.succez.handle;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.util.Parser;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * ����SelectionKey���ࡣ
 * 
 * @author succez
 *
 */
public class KeyHandler {
	private static final Logger LOG = LoggerFactory.getLogger(KeyHandler.class);

	public KeyHandler() {
	}

	/**
	 * ����SelectionKey���ж�key��������ͨ����״̬��
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void processKey(SelectionKey key) throws IOException {
		if (key.isValid() && key.isAcceptable()) {
			handleAccept(key);
		}
		if (key.isValid() && key.isReadable()) {
			handleRead(key);
		}
		if (key.isValid() && key.isWritable()) {
			handleWrite(key);
		}
	}

	// �����ͨ���Ѿ�׼���ý����µĿͻ������ӡ�����ע��Ŀͻ���ͨ��ע�ᵽѡ���������ø�ͨ��Ϊ��key����ΪOP_READ��
	private void handleAccept(SelectionKey key) throws IOException {
		// ��ȡ�ͻ���ͨ��
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		LOG.info("�յ�����" + socketChannel.socket().getRemoteSocketAddress()
				+ "������\n");
		// ����Ϊ������ģʽ������ע�ᵽѡ�����ϡ�
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ);
	}

	// �ͻ���ͨ���Ѿ�׼���ö�ȡ���ݵ��������С���ͨ���е����ݶ�ȡ���������У� Ȼ�� �Զ�ȡ���������е�HttpRequest���н����õ�Request��
	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		int bytesRead = socketChannel.read(buffer);
		if (bytesRead == -1) {
			/**
			 * ���һ���̹߳ر���ĳ���׽��ֵ�����ˣ���ͬʱ��һ���̱߳������ڸ��׽���ͨ���ϵĶ�ȡ�����У���ô���������߳��еĶ�ȡ��������ɣ�
			 * ������ȡ�κ��ֽ��ҷ��� -1������˵���ͻ������յ����ݺ�ر������ӡ�
			 */
			socketChannel.close();
			LOG.info("�ͻ��˹ر�������"
					+ socketChannel.socket().getRemoteSocketAddress() + "\n");
		} else if (bytesRead > 0) {
			LOG.info("��ͨ���е����ݶ��뵽��������\n");
			// �������ж����˿ͻ��˵�HttpRequest,��ȡ�������е�������Ϣ��
			buffer.flip();
			byte[] byteArray = buffer.array();
			String requestInfo = URLDecoder.decode(new String(byteArray),
					"utf-8");
			LOG.info("��ȡ" + socketChannel.socket().getRemoteSocketAddress()
					+ "��������Ϣ\n\n" + requestInfo);
			Request request = Parser.parse(requestInfo);// ��������õ�Request
			RequestHandler handler = new RequestHandler(request, socketChannel);
			Response response = null;
			try {
				response = handler.processRequest(socketChannel);
			} catch (CanNotHandleException e) {
				LOG.error("�������޷��������������");
			}
			key.attach(response);
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	/**
	 * �ͻ���ͨ���Ѿ�׼���ý����ݳ建����д��ͨ���С���ȡKey�ϸ��ӵ�Response���󣬽��ͻ������������д�뵽�������С�
	 */
	private void handleWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Response response = (Response) key.attachment();
		response.write(socketChannel);
		socketChannel.close();
	}
}
