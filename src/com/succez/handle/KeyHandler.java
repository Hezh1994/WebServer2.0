package com.succez.handle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import com.succez.appliction.ShowDirectory;
import com.succez.exception.CanNotHandleException;
import com.succez.util.ConfigReader;
import com.succez.util.Parser;
import com.succez.util.Seeker;
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
	 * 处理SelectionKey，判断key所关联的通道的状态
	 * 
	 * @param key
	 * @throws IOException
	 * @throws CanNotHandleException
	 */
	public void processKey(SelectionKey key) throws IOException,
			CanNotHandleException {
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
	private void handleAccept(SelectionKey key) throws IOException {
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
	private void handleRead(SelectionKey key) throws CanNotHandleException,
			IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
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
					encoding);
			LOG.info("获取" + socketChannel.socket().getRemoteSocketAddress()
					+ "的请求信息\n\n" + requestInfo);
			Request request = Parser.parse(requestInfo);// 解析请求得到Request
			request.setPort(socketChannel.socket().getLocalPort());
			if (!map.get("requestType").equals(request.getRequestType())) {
				throw new CanNotHandleException("无法处理的请求类型");
			}
			try {
				File file = Seeker.getFile(request.getUrl());
				if (file.isDirectory()) {
					// 展开目录，由展开目录的程序进行处理
					Response response = new Response(socketChannel);
					byte[] bytes = map.get("directoryHead").getBytes(encoding);
					socketChannel.write(ByteBuffer.wrap(bytes));
					ShowDirectory app = new ShowDirectory();
					app.service(request, response);
					socketChannel.close();
				} else {
					String s = file.getName();
					String suf = s.substring(s.indexOf(".") + 1, s.length());
					String imageType = map.get("imageType");
					if (imageType.contains(suf)) {
						// 访问的是图片
						byte[] bytes = map.get("image").getBytes(encoding);
						socketChannel.write(ByteBuffer.wrap(bytes));
						FileChannel channel = new FileInputStream(file)
								.getChannel();
						key.attach(channel);
						key.interestOps(SelectionKey.OP_WRITE);
					} else {
						byte[] bytes = map.get("fileHead").getBytes(encoding);
						socketChannel.write(ByteBuffer.wrap(bytes));
						FileChannel channel = new FileInputStream(file)
								.getChannel();
						key.attach(channel);
						key.interestOps(SelectionKey.OP_WRITE);
					}
				}
			} catch (FileNotFoundException e) {

			}
		}
	}

	/**
	 * 客户端通道已经准备好将数据从缓冲区写入到通道中。获取Key上附加的response对象，将客户端请求的数据写入到缓冲区中。
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void handleWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		FileChannel channel = (FileChannel) key.attachment();
		buffer.clear();
		int count = channel.read(buffer);
		if (count > 0) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				socketChannel.write(buffer);
			}
		} else {
			channel.close();
			socketChannel.close();
		}
	}
}
