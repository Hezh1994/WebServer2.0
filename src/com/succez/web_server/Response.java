package com.succez.web_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

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
					// 客户端访问目录时，展开该目录。
					expandDirectory(socketChannel, file);
				} catch (IOException e) {
					LOG.error("无法将数据从缓冲区写入到通道中");
				} catch (IsNotDirectory e) {
					LOG.error("试图展开一个非目录文件");
				}

			} else {
				try {
					// 客户端访问文件时，下载该文件
					downloadFile(socketChannel, file);
				} catch (IOException e) {

				}
			}
		} catch (FileNotFoundException e) {
			try {
				notFound(socketChannel);
			} catch (IOException e1) {
				LOG.error("无法将数据从缓冲区写入到通道中");
			}
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
		Writer.writeHeadToChannel("fileHead", socketChannel);
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
		Writer.writeHeadToChannel("directoryHead", socketChannel);
		StringBuilder sb = new StringBuilder(500);
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
			sb.append("<a href=\"http://localhost:"
					+ socketChannel.socket().getLocalPort() + request.getUrl()
					+ "/" + f.getName() + "\">");
			sb.append(f.getName() + "</a>");
			sb.append("</br>");
		}
		sb.append("</body>");
		sb.append("</html>");
		Writer.writeToChannel(sb.toString(), socketChannel);
	}

	/**
	 * 当客户端请求的资源不存在时，返回此页面。
	 * 
	 * @param socketChannel
	 *            通道
	 * @throws IOException
	 */
	private void notFound(SocketChannel socketChannel) throws IOException {
		Writer.writeHeadToChannel("notFound", socketChannel);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<h1>");
		sb.append("404 Not Found");
		sb.append("</h1>");
		sb.append("<h3>");
		sb.append("很抱歉,访问的资源不存在!请检查网址是否正确.");
		sb.append("</h3>");
		sb.append("</body>");
		sb.append("</html>");
		Writer.writeToChannel(sb.toString(), socketChannel);
	}
}
