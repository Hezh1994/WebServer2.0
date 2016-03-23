package com.succez.web_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.ConfigReader;
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
	public void write(SocketChannel socketChannel) throws CanNotHandleException {
		LOG.info("响应http请求");
		ConfigReader reader = ConfigReader.getConfigReader();
		Map<String, String> map = reader.getMap();
		if (!(map.get("requestType")).equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// 客户端访问目录时，展开该目录。
				expandDirectory(socketChannel, file);
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// 图片类型的文件，预览图片
					showPicture(socketChannel, file);
				} else {
					// 客户端访问文件时，下载该文件
					downloadFile(socketChannel, file);
				}
			}
		} catch (FileNotFoundException e) {
			// 文件不存在时，返回404 NotFound
			try {
				notFound(socketChannel);
			} catch (IOException e1) {
				LOG.error("文件下载失败");
			}
		}
	}

	/**
	 * 当客户端请求的资源类型为图片类型的文件时，提供图片的预览功能
	 * 
	 * @param socketChannel
	 * @param file
	 */
	private void showPicture(SocketChannel socketChannel, File file) {
		// TODO Auto-generated method stub
		try {
			Writer.writeHeadToChannel("directoryHead", socketChannel);
			Writer.writeToChannel(file, socketChannel);
		} catch (IOException e) {
			LOG.error("预览图片失败");
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
	private void downloadFile(SocketChannel socketChannel, File file) {
		try {
			Writer.writeHeadToChannel("fileHead", socketChannel);
			Writer.writeToChannel(file, socketChannel);
		} catch (IOException e) {

			LOG.error("下载文件失败!无法将数据写出到通道中");
		}
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
	private void expandDirectory(SocketChannel socketChannel, File file) {
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("展开目录失败!试图展开一个非目录文件");
		}
		try {
			Writer.writeHeadToChannel("directoryHead", socketChannel);
		} catch (IOException e) {
			LOG.error("展开目录失败!无法将数据写出到通道中");
		}
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
		try {
			Writer.writeToChannel(sb.toString(), socketChannel);
		} catch (IOException e) {
			LOG.error("展开目录失败!无法将数据写出到通道中");
		}
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
		StringBuilder sb = new StringBuilder(500);
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
