package com.succez.web_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.handle.Handler;

/**
 * ��������ʼ�����������ṩ��������start��shutDown����
 * 
 * @author succez
 *
 */
public class Server {
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	private static final int TIME_OUT = 5000;
	private int port;
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;

	/**
	 * ���췽��,Ĭ�϶˿ں�Ϊ80.</br>
	 * <p>
	 * 1.��ѡ����.
	 * </p>
	 * <p>
	 * 2.�򿪷������׽���ͨ��.
	 * </p>
	 * <p>
	 * 3.�׽��ְ󶨵�ָ���Ķ˿�.
	 * </p>
	 * <p>
	 * 5.���÷������׽���ͨ��Ϊ������ģʽ.
	 * </p>
	 * <p>
	 * 6.����ѡ����ע���ͨ��.
	 * </p>
	 * 
	 * @throws IOException
	 *             ��������ʼ��ʧ��,����IO����ʱ,�׳����쳣.
	 */
	public Server() throws IOException {
		super();
		this.port = 80;
		this.selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
		this.serverSocketChannel.configureBlocking(false);
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * ���ϵ���ѯselect��������ȡ׼�����˵�ͨ����������key������ȡkey���е�ÿһ��key���ж�key������ͨ������Ȥ�Ĳ�����������Ӧ�Ĵ���
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		LOG.info("����������,���ڼ����˿ں�:" + port);
		while (true) {
			if (selector.select(TIME_OUT) == 0) {
				LOG.info("�ȴ�����...");
				continue;
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			Handler handler = new Handler(4096);
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				if (key.isAcceptable()) {
					handler.handleAccept(key);
				}
				if (key.isReadable()) {
					handler.handleRead(key);
				}
				if (key.isWritable()) {
					handler.handleWrite(key);
				}
				iterator.remove();
			}
		}
	}
}
