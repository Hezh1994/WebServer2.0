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
 * ʵ����Handle�ӿڵĴ��������ܶ�SelectionKey���д�������һ����������
 * 
 * @author succez
 *
 */
public class Handler implements Handle {
	private static final Logger LOG = LoggerFactory.getLogger(Handler.class);
	private int bufferSize;

	public Handler(int bufferSize) {
		super();
		this.bufferSize = bufferSize;
	}

	/**
	 * �����ͨ���Ѿ�׼���ý����µĿͻ������ӡ�����ע��Ŀͻ���ͨ��ע�ᵽѡ���������ø�ͨ��Ϊ��key����ΪOP_READ,�������������ӵ��˼���
	 */
	public void handleAccept(SelectionKey key) throws IOException {
		// ��ȡ�ͻ���ͨ��
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		LOG.info("�յ�����" + socketChannel.socket().getRemoteSocketAddress()
				+ "������\n");
		// ����Ϊ������ģʽ������ע�ᵽѡ�����ϡ�
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ,
				ByteBuffer.allocate(bufferSize));
	}

	/**
	 * �ͻ���ͨ���Ѿ�׼���ö�ȡ���ݵ��������С���ȡ��ͨ�������ӵĶ��󼴻���������ͨ���е����ݶ�ȡ���������С�Ȼ��
	 * �Զ�ȡ���������е�HttpRequest���н����õ�Request��
	 */
	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// ��ȡ���ŵ��������ĸ���������Ϊ������
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
			String request = URLDecoder.decode(new String(byteArray), "utf-8");
			LOG.info("�յ�����" + socketChannel.socket().getRemoteSocketAddress()
					+ "������\n\n" + request);
			key.attach(request);
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	/**
	 * �ͻ���ͨ���Ѿ�׼���ý����ݳ建����д��ͨ���С���ȡkey���ӵ�������Ϣ���Ի������е�HttpRequest���н�������Ӧ�ͻ��˵�����
	 * ���ͻ����������Դд��ͨ����
	 */
	public void handleWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// ��ȡ������key�ϵĻ�������������HttpRequest��
		String requestInfo = (String) key.attachment();
		LOG.info("����Http����");
		Request request = Parser.parse(requestInfo);
		LOG.info("��ӦHttp����");
		Response response = new Response(request);
		try {
			response.response(socketChannel);
		} catch (CanNotHandleException e) {
			LOG.error("�������޷����������");
		}
		socketChannel.close();
	}
}
