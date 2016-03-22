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
 * 创建并初始化服务器，提供服务器的start和shutDown方法.
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
	 * 不断的轮询select方法，获取准备好了的通道所关联的key集。获取key集中的每一个key，判断key关联的通道感兴趣的操作并进行相应的处理。
	 * 
	 * @throws IOException
	 */
	public void run() {
		LOG.info("服务器启动,正在监听端口号:" + port);
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
			Handler handler = new Handler(4096);
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				if (key.isValid() && key.isAcceptable()) {
					try {
						handler.handleAccept(key);
					} catch (IOException e) {
						LOG.error("套接字通道发生IO错误");
					}
				}
				if (key.isValid() && key.isReadable()) {
					try {
						handler.handleRead(key);
					} catch (IOException e) {
						LOG.error("套接字通道发生IO错误");
					}
				}
				if (key.isValid() && key.isWritable()) {
					try {
						handler.handleWrite(key);
					} catch (IOException e) {
						LOG.error("套接字通道发生IO错误");
					}
				}
			}
		}
	}

	/**
	 * 关闭选择器以及服务端套接字通道
	 */
	public void shutDown() {
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				LOG.error("无法正常关闭选择器");
			}
		}
		if (serverSocketChannel != null) {
			try {
				serverSocketChannel.close();
			} catch (IOException e) {
				LOG.error("无法正常关闭服务端套接字通道");
			}
		}
	}
}
