package com.succez.handle;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.UnableConnectException;

/**
 * 处理SelectionKey的类
 * 
 * @author succez
 *
 */
public class KeyHandler {
	private static final Logger LOG = LoggerFactory.getLogger(KeyHandler.class);
	private ThreadPoolExecutor executor;

	public KeyHandler() {
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(20);
		executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
				queue);
	}

	/**
	 * 处理SelectionKey，判断key所关联的通道的状态，分别进行不同的处理。
	 * 
	 * @param key
	 * @throws CanNotHandleException
	 * @throws UnableConnectException
	 * @throws ReadIOException
	 * @throws WriteIOException
	 */
	public void processKey(SelectionKey key) throws UnableConnectException {
		if (key.isValid() && key.isAcceptable()) {
			handleAccept(key);
		}
		if (key.isValid() && key.isReadable()) {
			// 客户端通道可读，启动读线程，读取客户端中的请求信息，并对信息进行处理。
			executor.execute(new ReadThread(key));
		}
		if (key.isValid() && key.isWritable()) {
			executor.execute(new WriteHandle(key));
		}
	}

	// 服务端通道已经准备好接受新的客户端连接。将新注册的客户端通道注册到选择器，设置该通道关联的key的属性为OP_READ
	private void handleAccept(SelectionKey key) throws UnableConnectException {
		// 获取客户端通道
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			LOG.info("收到来自" + socketChannel.socket().getRemoteSocketAddress()
					+ "的连接\n");
			// 设置为非阻塞模式才能注册到选择器上
			socketChannel.configureBlocking(false);
			socketChannel.register(key.selector(), SelectionKey.OP_READ);
		} catch (Exception e) {
			throw new UnableConnectException("无法与客户端建立连接");
		}
	}
}
