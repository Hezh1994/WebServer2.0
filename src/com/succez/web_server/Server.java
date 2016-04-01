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
import com.succez.handle.KeyHandler;
import com.succez.util.ConfigReader;

/**
 * 创建并初始化服务器，提供服务器的start和shutDown方法
 * 
 * @author succez
 *
 */
public class Server extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	private static final int TIME_OUT = 10000;
	private int port;
	private boolean flag;
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	/**
	 * 构造方法，默认端口号为80</br>
	 * <p>
	 * 1.打开选择器
	 * </p>
	 * <p>
	 * 2.打开服务器套接字通道
	 * </p>
	 * <p>
	 * 3.将套接字绑定到指定的端口
	 * </p>
	 * <p>
	 * 4.设置服务器套接字通道为非阻塞模式
	 * </p>
	 * <p>
	 * 5.向选择器注册套该通道
	 * </p>
	 * 
	 * @throws IOException
	 *             服务器初始化失败，出现I/O错误，抛出该异常
	 */
	public Server() throws IOException {
		super();
		this.flag = true;
		ConfigReader reader = ConfigReader.getConfigReader();
		this.port = Integer.valueOf(reader.getMap().get("port"));
		this.selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
		this.serverSocketChannel.configureBlocking(false);
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * 不断的轮询select方法，获取准备好进行I/O操作的通道所关联的key集。
	 */
	public void run() {
		LOG.info("服务器启动，正在监听端口号：" + port);
		while (flag) {
			try {
				if (selector.select(TIME_OUT) == 0) {
					LOG.info("等待连接...");
					continue;
				}
			} catch (IOException e) {
				LOG.error("选择器无法正常工作");
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					KeyHandler.processKey(key);
				} catch (IOException e) {
					LOG.error("无法处理SelectionKey：发生I/O错误");
				}
			}
		}
	}

	/**
	 * 关闭选择器以及服务端套接字通道
	 * 
	 * @throws IOException
	 *             服务器无法正常关闭
	 */
	public void shutDown() throws IOException {
		LOG.info("关闭服务器");
		if (selector != null) {
			selector.close();
		}
		if (serverSocketChannel != null) {
			serverSocketChannel.close();
		}
	}
}
