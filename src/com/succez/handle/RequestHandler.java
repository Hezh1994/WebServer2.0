package com.succez.handle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.appliction.ShowDirectory;
import com.succez.exception.CanNotHandleException;
import com.succez.util.ConfigReader;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 处理Request的类
 * 
 * @author succez
 *
 */
public class RequestHandler {
	private static final Logger LOG = LoggerFactory
			.getLogger(RequestHandler.class);
	private Request request;
	private SelectionKey key;
	private SocketChannel socketChannel;
	private Map<String, String> map;
	private String encoding;

	/**
	 * 构造方法，读取配置文件信息
	 * 
	 * @param request
	 * @param socketChannel
	 */
	public RequestHandler(Request request, SelectionKey key) {
		super();
		this.request = request;
		this.key = key;
		this.socketChannel = (SocketChannel) key.channel();
		ConfigReader reader = ConfigReader.getConfigReader();
		this.map = reader.getMap();
		this.encoding = map.get("encoding");
	}

	/**
	 * 处理request
	 * 
	 * @throws CanNotHandleException
	 *             请求类型无法处理时，抛出该异常
	 * @throws UnsupportedEncodingException
	 *             把字符串编码为utf-8字节数组失败时，抛出该异常
	 * @throws IOException
	 *             无法将应答头信息写入通道时，抛出该异常
	 */
	public void processHandler() throws CanNotHandleException,
			UnsupportedEncodingException, IOException {
		LOG.info("处理请求");
		if (!map.get("requestType").equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		String head = null;
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				LOG.info("展开目录");
				// 请求访问的是一个目录，将请求交给展开目录的应用进行处理
				head = map.get("directoryHead");
				writeHead(file, head);
				Response response = new Response(socketChannel);
				ShowDirectory.service(request, response);
				socketChannel.close();
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// 访问的是图片
					LOG.info("预览图片");
					head = map.get("image");
					writeHead(file, head);

				} else {
					// 访问的是文件
					LOG.info("下载文件");
					head = map.get("fileHead");
					writeHead(file, head);
				}
			}
		} catch (FileNotFoundException e) {
			// 访问的资源不存在
			LOG.info("资源不存在");
			File file = null;
			file = new File(map.get("errorFilePath"));
			head = map.get("notFound");
			writeHead(file, head);
		}
	}

	/**
	 * 把Http应答头写入到通道中，如果请求的是文件资源，则将文件输入流附加在key上并把通道感兴趣的操作设置为OP_WRITE
	 * 
	 * @param file
	 *            访问的文件
	 * @param head
	 *            应答头
	 * @throws UnsupportedEncodingException
	 *             把字符串编码为utf-8字节数组失败时，抛出该异常
	 * @throws IOException
	 *             无法将应答头信息写入通道时，抛出该异常
	 */
	private void writeHead(File file, String head)
			throws UnsupportedEncodingException, IOException {
		if (!file.isDirectory()) {
			byte[] bytes = head.getBytes(encoding);
			socketChannel.write(ByteBuffer.wrap(bytes));
			FileInputStream is = new FileInputStream(file);
			key.attach(is);
			key.interestOps(SelectionKey.OP_WRITE);
		} else {
			byte[] bytes = head.getBytes(encoding);
			socketChannel.write(ByteBuffer.wrap(bytes));
		}
	}

}
