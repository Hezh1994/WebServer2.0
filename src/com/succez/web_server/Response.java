package com.succez.web_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.Seeker;
import com.succez.util.Writer;

/**
 * 响应客户端的http请求，将客户端请求的资源写入到通道中。
 * 
 * @author succez
 *
 */
public class Response {
	private static final Logger LOG = LoggerFactory.getLogger(Response.class);
	private Request request;

	public Response(Request request) {
		super();
		this.request = request;
	}

	/**
	 * 响应客户端的Http请求，将客户端所请求的资源写入到通道中，目前只能处理GET请求。若客户端请求的资源类型是一个目录，
	 * 则返回一个展示该目录中所有文件或文件夹的html，
	 * 若客户端请求的是文件类型，则会下载该文件；若客户端请求的是图片类型的文件，则可以在线预览该图片。
	 * 
	 * @param socketChannel
	 *            客户端通道
	 * @throws CanNotHandleException
	 *             请求无法处理时，抛出该异常
	 */
	public void response(SocketChannel socketChannel)
			throws CanNotHandleException {
		LOG.info("响应http请求");
		if (!"GET".equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				try {
					expandDirectory(socketChannel, file);
				} catch (IOException e) {
					LOG.error("无法将数据从缓冲区写入到通道中");
				} catch (IsNotDirectory e) {
					LOG.error("试图展开一个非目录文件");
				}

			} else {
				try {
					downloadFile(socketChannel, file);
				} catch (IOException e) {
					LOG.error("无法将数据从缓冲区写入到通道中");
				}
			}
		} catch (FileNotFoundException e) {
		}
	}

	/**
	 * 当客户端请求的资源时一个文件时，则提供下载。将http应答头和文件的字节数组包装到ByteBuffer中，并从缓冲区写入到通道中。
	 * 
	 * @param socketChannel
	 *            客户端通道
	 * @param file
	 *            请求的文件
	 * @param buffer
	 *            缓冲区
	 * @throws IOException
	 *             发生IO错误时，无法将数据从缓冲区写入通道中时，抛出该异常
	 */
	private void downloadFile(SocketChannel socketChannel, File file)
			throws IOException {
		Properties properties = new Properties();
		InputStream is = this.getClass().getResourceAsStream(
				"httpResponse.properties");
		properties.load(is);
		is.close();
		String fileHead = properties.getProperty("fileHead");
		Writer.writeToChannel(fileHead, socketChannel);
		Writer.writeToChannel(file, socketChannel);
	}

	/**
	 * 当客户端请求的资源是一个目录时，则展开该目录。展示该目录下所有文件，以html的形式返回给客户端。
	 * 
	 * @param socketChannel
	 * @param file
	 * @param buffer
	 * @throws IOException
	 * @throws IsNotDirectory
	 */
	private void expandDirectory(SocketChannel socketChannel, File file)
			throws IOException, IsNotDirectory {
		List<File> files = Seeker.getFiles(file);
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\nConnection:keep-alive\r\nServer:WebServer\r\n\r\n");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<p style=\"color:blue\">当前路径" + request.getUrl()
				+ "下的内容为:</p>");
		for (File f : files) {
			sb.append("<a href=\"http://192.168.13.157:"
					+ socketChannel.socket().getLocalPort() + request.getUrl()
					+ "/" + f.getName() + "\">");
			sb.append(f.getName() + "</a>");
			sb.append("</br>");
		}
		sb.append("</body>");
		sb.append("</html>");
		socketChannel.write(ByteBuffer.wrap(sb.toString().getBytes("utf-8")));
	}
}
