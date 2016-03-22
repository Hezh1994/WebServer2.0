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
import com.succez.util.ConfigReader;

/**
 * ��������ʼ�����������ṩ��������start��shutDown����.
 * 
 * @author succez
 *
 */
public class Server extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	private static final int TIME_OUT = 5000;
	private int port;
	private boolean flag;
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

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
		this.flag = true;
		ConfigReader reader = ConfigReader.getConfigReader();
		this.port = Integer.valueOf(reader.getMap().get("port"));
		this.selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.socket().bind(
				new InetSocketAddress(Integer.valueOf(port)));
		this.serverSocketChannel.configureBlocking(false);
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * ���ϵ���ѯselect��������ȡ׼�����˵�ͨ����������key������ȡkey���е�ÿһ��key���ж�key������ͨ������Ȥ�Ĳ�����������Ӧ�Ĵ���
	 * 
	 * @throws IOException
	 */
	public void run() {
		LOG.info("����������,���ڼ����˿ں�:" + port);
		while (flag) {
			try {
				if (selector.select(TIME_OUT) == 0) {
					LOG.info("�ȴ�����...");
					continue;
				}
			} catch (IOException e) {
				LOG.error("ѡ�����޷���������");
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			Handler handler = new Handler(4096);
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				if (key.isValid() && key.isAcceptable()) {
					try {
						handler.handleAccept(key);
					} catch (IOException e) {
						LOG.error("�׽���ͨ������IO����");
					}
				}
				if (key.isValid() && key.isReadable()) {
					try {
						handler.handleRead(key);
					} catch (IOException e) {
						LOG.error("�׽���ͨ������IO����");
					}
				}
				if (key.isValid() && key.isWritable()) {
					try {
						handler.handleWrite(key);
					} catch (IOException e) {
						LOG.error("�׽���ͨ������IO����");
					}
				}
			}
		}
	}

	/**
	 * �ر�ѡ�����Լ�������׽���ͨ��
	 */
	public void shutDown() {
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				LOG.error("�޷������ر�ѡ����");
			}
		}
		if (serverSocketChannel != null) {
			try {
				serverSocketChannel.close();
			} catch (IOException e) {
				LOG.error("�޷������رշ�����׽���ͨ��");
			}
		}
	}
}
