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

import com.succez.util.Process;

public class Server {
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	private static final int TIME_OUT = 5000;
	private int port;
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;

	/**
	 * 构造方法,默认端口号为80.</br>
	 * <p>
	 * 1.打开选择器.
	 * </p>
	 * <p>
	 * 2.打开服务器套接字通道.
	 * </p>
	 * <p>
	 * 3.套接字绑定到指定的端口.
	 * </p>
	 * <p>
	 * 5.设置服务器套接字通道为非阻塞模式.
	 * </p>
	 * <p>
	 * 6.并向选择器注册该通道.
	 * </p>
	 * 
	 * @throws IOException
	 *             服务器初始化失败,出现IO错误时,抛出该异常.
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
	 * 启动服务器，服务器轮询，等待客户端的连接，超时时长为10秒。I/O通道操作准备就绪后，将其对应的键传递给
	 * {@link com.succez.util.Process#process process}进行处理
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		LOG.info("服务器启动,正在监听端口号:" + port);
		while (true) {
			if (selector.select(TIME_OUT) == 0) {
				LOG.info("等待连接...");
				continue;
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				Process.process(iterator.next());
				iterator.remove();
			}
		}
	}
}
