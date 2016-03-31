package com.succez.handle;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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

	/**
	 * 处理SelectionKey，判断key所关联的通道的状态，分别进行不同的处理。
	 * 
	 * @param key
	 * @throws CanNotHandleException
	 * @throws UnableConnectException
	 * @throws ReadIOException
	 * @throws WriteIOException
	 */
	public static void processKey(SelectionKey key)
			throws UnableConnectException {
		if (key.isValid() && key.isAcceptable()) {
			// 服务端通道已经准备好接受新的客户端连接。将新注册的客户端通道注册到选择器，设置该通道关联的key的属性为OP_READ

			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
					.channel();
			try {
				SocketChannel socketChannel = serverSocketChannel.accept();
				LOG.info("收到来自"
						+ socketChannel.socket().getRemoteSocketAddress()
						+ "的连接\n");
				// 设置为非阻塞模式才能注册到选择器上
				socketChannel.configureBlocking(false);
				socketChannel.register(key.selector(), SelectionKey.OP_READ);
			} catch (Exception e) {
				throw new UnableConnectException("无法与客户端建立连接");
			}
		}
		if (key.isValid() && key.isReadable()) {
			new ReadThread(key).run();
		}
		if (key.isValid() && key.isWritable()) {

			new WriteHandle(key).run();
		}
	}

}
