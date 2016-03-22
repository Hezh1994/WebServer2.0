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
 * 实现了Handle接口的处理器，能对SelectionKey进行处理，内置一个缓冲区。
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
	 * 服务端通道已经准备好接受新的客户端连接。将新注册的客户端通道注册到选择器，设置该通道为的key属性为OP_READ,并将缓冲区附加到此键。
	 */
	public void handleAccept(SelectionKey key) throws IOException {
		// 获取客户端通道
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		LOG.info("收到来自" + socketChannel.socket().getRemoteSocketAddress()
				+ "的连接\n");
		// 设置为非阻塞模式，才能注册到选择器上。
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ,
				ByteBuffer.allocate(bufferSize));
	}

	/**
	 * 客户端通道已经准备好读取数据到缓冲区中。获取该通道所附加的对象即缓冲区，将通道中的数据读取到缓冲区中。然后，
	 * 对读取到缓冲区中的HttpRequest进行解析得到Request。
	 */
	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// 获取该信道所关联的附件，这里为缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		int bytesRead = socketChannel.read(buffer);
		if (bytesRead == -1) {
			/**
			 * 如果一个线程关闭了某个套接字的输入端，而同时另一个线程被阻塞在该套接字通道上的读取操作中，那么处于阻塞线程中的读取操作将完成，
			 * 而不读取任何字节且返回 -1。这里说明客户端在收到数据后关闭了连接。
			 */
			socketChannel.close();
			LOG.info("客户端关闭了连接"
					+ socketChannel.socket().getRemoteSocketAddress() + "\n");
		} else if (bytesRead > 0) {
			LOG.info("将通道中的数据读入到缓冲区中\n");
			// 缓冲区中读到了客户端的HttpRequest,获取缓冲区中的请求信息。
			buffer.flip();
			byte[] byteArray = buffer.array();
			String request = URLDecoder.decode(new String(byteArray), "utf-8");
			LOG.info("收到来自" + socketChannel.socket().getRemoteSocketAddress()
					+ "的请求\n\n" + request);
			key.attach(request);
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	/**
	 * 客户端通道已经准备好将数据冲缓冲区写入通道中。获取key附加的请求信息，对缓冲区中的HttpRequest进行解析并响应客户端的请求，
	 * 将客户端请求的资源写入通道。
	 */
	public void handleWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// 获取附加在key上的缓冲区，里面有HttpRequest。
		String requestInfo = (String) key.attachment();
		LOG.info("解析Http请求");
		Request request = Parser.parse(requestInfo);
		LOG.info("响应Http请求");
		Response response = new Response(request);
		try {
			response.response(socketChannel);
		} catch (CanNotHandleException e) {
			LOG.error("服务器无法处理该请求");
		}
		socketChannel.close();
	}
}
