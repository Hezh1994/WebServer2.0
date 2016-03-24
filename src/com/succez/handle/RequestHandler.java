package com.succez.handle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.CanNotTranslateException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.ConfigReader;
import com.succez.util.FileToByte;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 处理Request，得到一个Response。
 * 
 * @author succez
 *
 */
public class RequestHandler {
	private static final Logger LOG = LoggerFactory
			.getLogger(RequestHandler.class);
	private Request request;
	private SocketChannel socketChannel;
	private String encoding;
	private Map<String, String> map;

	/**
	 * 读取配置分件，得到map集合，设置编码格式。
	 * 
	 * @param request
	 * @param socketChannel
	 */
	public RequestHandler(Request request, SocketChannel socketChannel) {
		super();
		this.request = request;
		this.socketChannel = socketChannel;
		ConfigReader reader = ConfigReader.getConfigReader();
		this.map = reader.getMap();
		this.encoding = map.get("encoding");
	}

	/**
	 * 处理客户端请求，获取请求资源的字节数组，将Http应答头和请求的资源包装到Response中。
	 * 
	 * @param socketChannel
	 *            客户端通道
	 * @return Response
	 * @throws CanNotHandleException
	 *             请求类型无法处理时，抛出该异常
	 */
	public Response processRequest(SocketChannel socketChannel)
			throws CanNotHandleException {
		LOG.info("处理请求");
		if (!(map.get("requestType")).equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		byte[] bytes = null;
		String responseHead;
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// 客户端访问目录时，展开该目录。
				responseHead = map.get("directoryHead");
				bytes = expandDirectory(file);
				Response response = new Response(responseHead, bytes);
				return response;
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// 图片类型的文件，预览图片
					responseHead = map.get("directoryHead");
					try {
						bytes = FileToByte.fileToByte(file);
					} catch (Exception e) {
						LOG.error("无法预览" + file.getName() + ":读取文件失败");
					}
					return new Response(responseHead, bytes);

				} else {
					Response response = null;
					// 客户端访问文件时，下载该文件
					try {
						response = downloadFile(file);
					} catch (Exception e) {
						LOG.error("下载文件" + file.getName() + "失败:无法读取文件");
					}
					return response;
				}
			}
		} catch (FileNotFoundException e) {
			// 文件不存在时，返回404 NotFound
			responseHead = map.get("notFound");
			bytes = returnNotFound();
			return new Response(responseHead, bytes);
		}
	}

	private Response downloadFile(File file) throws CanNotTranslateException,
			IOException {
		if (request.getRang() == null) {
			// 全部下载
			String responseHead = map.get("fileHead");
			byte[] bytes = FileToByte.fileToByte(file);
			return new Response(responseHead, bytes);
		} else {
			// 断点续传
			LOG.info("开始断点续传");

			return null;
		}
	}

	/**
	 * 客户端请求的资源不存在时，返回该页面。
	 * 
	 * @return
	 */
	private byte[] returnNotFound() {
		StringBuilder sb = new StringBuilder(500);
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<p>");
		sb.append("404 Not Found");
		sb.append("</p>");
		sb.append("<p>");
		sb.append("很抱歉,访问的资源不存在!请检查网址是否正确.");
		sb.append("</p>");
		sb.append("</body>");
		sb.append("</html>");
		byte[] bytes = null;
		try {
			bytes = sb.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("无法返回404:不支持的编码类型");
		}
		return bytes;
	}

	/**
	 * 将目录中的内容写入到html页面中，并将该html以字节数据形式返回。
	 * 
	 * @param file
	 */
	private byte[] expandDirectory(File file) {
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("展开目录失败!试图展开一个非目录文件");
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
			sb.append("<br />");
		}
		sb.append("</body>");
		sb.append("</html>");
		byte[] bytes = null;
		try {
			bytes = sb.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("无法展开目录:不支持的编码类型");
		}
		return bytes;
	}
}
