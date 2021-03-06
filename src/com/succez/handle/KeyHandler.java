package com.succez.handle;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.appliction.HttpAppliction;
import com.succez.util.Parser;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 处理SelectionKey的类
 * 
 * @author succez
 *
 */
public class KeyHandler {
	private static final Logger LOG = LoggerFactory.getLogger(KeyHandler.class);
	private static final int BUFFER_SIZE = 4096;

	/**
	 * 处理SelectionKey，判断key所关联的通道的状态
	 * 
	 * @param key
	 * @throws IOException
	 */
	public static void processKey(SelectionKey key) throws IOException {
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

	// 服务端通道已经准备好接受新的客户端连接。将新注册的客户端通道注册到选择器，设置该通道关联的key的属性为OP_READ
	private static void handleAccept(SelectionKey key) throws IOException {
		// 获取客户端通道
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		LOG.info("收到来自" + socketChannel.socket().getRemoteSocketAddress()
				+ "的连接\n");
		// 设置为非阻塞模式才能注册到选择器上
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ);
	}

	// 客户端通道已经准备好读取数据到缓冲区中。将通道中的数据读取到缓冲区中，然后对读取到缓冲区中的HttpRequest进行解析得到Request
	private static void handleRead(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.clear();
		int bytesRead = socketChannel.read(buffer);
		if (bytesRead == -1) {
			/**
			 * 如果一个线程关闭了某个套接字的输入端，而同时另一个线程被阻塞在该套接字通道的读取操作中，
			 * 那么处于阻塞线程中的读取操作将完成而不读取任何 字节且返回-1。这里说明客户端在收到数据后关闭了连接。
			 */
			socketChannel.close();
			LOG.info("客户端关闭了连接"
					+ socketChannel.socket().getRemoteSocketAddress() + "\n");
		} else if (bytesRead > 0) {
			LOG.info("将通道中的数据读入到缓冲区中\n");
			// 缓冲区中读到了客户端的请求信息，获取缓冲区中的请求信息
			buffer.flip();
			byte[] byteArray = buffer.array();
			String requestInfo = URLDecoder.decode(new String(byteArray),
					"utf-8");
			LOG.info("获取" + socketChannel.socket().getRemoteSocketAddress()
					+ "的请求信息\n\n" + requestInfo);
			Request request = Parser.parse(requestInfo);// 解析请求得到RequestS
			key.attach(request);
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	/**
	 * 客户端通道已经准备好将数据从缓冲区写入到通道中。获取Key上附加的response对象，将客户端请求的数据写入到缓冲区中。
	 * 
	 * @param key
	 * @throws IOException
	 */
	private static void handleWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Request request = (Request) key.attachment();
		HttpAppliction app = new HttpAppliction();
		Response response = new Response(socketChannel);
		ResponseThread thread = new ResponseThread(app, request, response,
				socketChannel);
		thread.start();
		key.cancel();
	}
}
