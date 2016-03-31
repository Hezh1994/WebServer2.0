package com.succez.handle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.util.ConfigReader;
import com.succez.util.Parser;
import com.succez.web_server.Request;

/**
 * 当通道对应的状态为可读时，启动该线程，完成对该通道中信息的读取、解析、查找资源并得到文件的输入流的操作
 * 
 * @author succez
 *
 */
public class ReadThread implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(ReadThread.class);
	private SelectionKey key;
	private String encoding;

	public ReadThread(SelectionKey key) {
		super();
		this.key = key;
		ConfigReader reader = ConfigReader.getConfigReader();
		encoding = reader.getMap().get("encoding");

	}

	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		SocketChannel socket = (SocketChannel) key.channel();
		try {
			int bytesRead = socket.read(buffer);
			if (bytesRead == -1) {
				/**
				 * 如果一个线程关闭了某个套接字的输入端，而同时另一个线程被阻塞在该套接字通道的读取操作中，
				 * 那么处于阻塞线程中的读取操作将完成而不读取任何 字节且返回-1。这里说明客户端在收到数据后关闭了连接。
				 */
				socket.close();
				LOG.info("客户端关闭了连接" + socket.socket().getRemoteSocketAddress()
						+ "\n");
			} else if (bytesRead > 0) {
				LOG.info("通道可读，将通道中的数据读取到缓冲区中。\n");
				// 缓冲区中读到了客户端的请求信息，获取缓冲区中的请求信息
				buffer.flip();
				byte[] byteArray = buffer.array();
				String requestInfo = null;
				try {
					requestInfo = URLDecoder.decode(new String(byteArray),
							encoding);
				} catch (UnsupportedEncodingException e) {
					LOG.info("读取客户端请求信息失败：不支持的编码格式");
				}
				LOG.info("读取" + socket.socket().getRemoteSocketAddress()
						+ "的请求信息\n\n" + requestInfo);
				Request request = Parser.parse(requestInfo);// 解析请求信息得到Request对象
				request.setPort(socket.socket().getLocalPort());
				RequestHandler handler = new RequestHandler(request, key);
				handler.processHandler(); // 处理请求
			}
		} catch (IOException e) {
			LOG.info("读取信息失败：发生I/O错误");
		} catch (CanNotHandleException e) {
			LOG.info("不支持的请求类型，只支持GET请求方式");
		}
	}
}
