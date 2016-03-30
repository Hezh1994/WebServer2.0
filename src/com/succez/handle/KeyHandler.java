package com.succez.handle;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.ReadIOException;
import com.succez.exception.UnableConnectException;
import com.succez.exception.WriteIOException;
import com.succez.util.ConfigReader;
import com.succez.util.Parser;
import com.succez.web_server.Request;

/**
 * 处理SelectionKey的类
 * 
 * @author succez
 *
 */
public class KeyHandler {
	private static final Logger LOG = LoggerFactory.getLogger(KeyHandler.class);
	private ByteBuffer buffer;
	private Map<String, String> map;
	private String encoding;

	public KeyHandler(int BufferSize) {
		this.buffer = ByteBuffer.allocate(BufferSize);
		ConfigReader reader = ConfigReader.getConfigReader();
		this.map = reader.getMap();
		this.encoding = map.get("encoding");
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
	public void processKey(SelectionKey key) throws CanNotHandleException,
			UnableConnectException, ReadIOException, WriteIOException {
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

	// 客户端通道已经准备好读取数据到缓冲区中。将通道中的数据读取到缓冲区中，然后对读取到缓冲区中的HttpRequest进行解析得到Request
	private void handleRead(SelectionKey key) throws CanNotHandleException,
			ReadIOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buffer.clear();
		try {
			int bytesRead = socketChannel.read(buffer);
			if (bytesRead == -1) {
				/**
				 * 如果一个线程关闭了某个套接字的输入端，而同时另一个线程被阻塞在该套接字通道的读取操作中，
				 * 那么处于阻塞线程中的读取操作将完成而不读取任何 字节且返回-1。这里说明客户端在收到数据后关闭了连接。
				 */
				socketChannel.close();
				LOG.info("客户端关闭了连接"
						+ socketChannel.socket().getRemoteSocketAddress()
						+ "\n");
			} else if (bytesRead > 0) {
				LOG.info("将通道中的数据读入到缓冲区中\n");
				// 缓冲区中读到了客户端的请求信息，获取缓冲区中的请求信息
				buffer.flip();
				byte[] byteArray = buffer.array();
				String requestInfo = URLDecoder.decode(new String(byteArray),
						encoding);
				LOG.info("获取" + socketChannel.socket().getRemoteSocketAddress()
						+ "的请求信息\n\n" + requestInfo);
				Request request = Parser.parse(requestInfo);// 解析请求信息得到Request对象
				request.setPort(socketChannel.socket().getLocalPort());
				RequestHandler handler = new RequestHandler(request, key);
				handler.processHandler(); // 处理请求
			}
		} catch (IOException e) {
			throw new ReadIOException("通道可读时，发生I/O错误");
		}
	}

	/**
	 * 客户端通道已经准备好将数据从缓冲区写入到通道中。获取key上附加的文件输入流对象，将文件写入通道中，返回给客户端。
	 * 为防止主线程阻塞在写入操作中而无法继续轮询处理其他通道，一次只写入文件的一部分，无论写入多少，写入后立刻返回。
	 * 
	 * @param key
	 * @throws WriteIOException
	 *             向客户端写入文件时发生错误
	 */
	private void handleWrite(SelectionKey key) throws WriteIOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		FileInputStream is = (FileInputStream) key.attachment();
		FileChannel channel = is.getChannel();
		buffer.clear();
		try {
			int count = channel.read(buffer);
			if (count > 0) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					try {
						socketChannel.write(buffer);
					} catch (IOException e) {
						LOG.info("客户端取消了下载");
						is.close();
						channel.close();
						socketChannel.close();
						break;
					}
				}
			} else {
				is.close();
				channel.close();
				socketChannel.close();
			}
		} catch (Exception e) {
			throw new WriteIOException();
		}
	}
}
