package com.succez.handle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.ConfigReader;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 处理Request，得到一个Response
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
	 * 读取配置文件，得到map集合，设置编码格式
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
	 * 处理客户端请求，获取请求资源的字节数组，将http应答头和请求的资源包装到response中。
	 * 
	 * @param socketChannel
	 *            客户端通道
	 * @return
	 * @throws CanNotHandleException
	 *             请求无法处理时，抛出该异常。
	 */
	public Response processRequest(SocketChannel socketChannel)
			throws CanNotHandleException {
		LOG.info("处理请求");
		if (!(map.get("requestType")).equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		InputStream is = null;
		byte[] responseHead;
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// 客户端访问目录时，展开该目录
				responseHead = getResponseHead("directoryHead");
				byte[] bytes = expandDirectory(file);
				long fileLength = bytes.length;
				is = new ByteArrayInputStream(bytes);
				Response response = new Response(responseHead, fileLength, is);
				return response;
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// 图片类型的文件，预览图片
					responseHead = getResponseHead("image");
					is = new FileInputStream(file);
					return new Response(responseHead, file.length(), is);

				} else {
					// 下载文件
					responseHead = getResponseHead("fileHead");
					is = new FileInputStream(file);
					return new Response(responseHead, file.length(), is);
				}
			}
		} catch (FileNotFoundException e) {
			// 文件不存在时，返回404 NotFound
			responseHead = getResponseHead("notFound");
			File file = null;
			try {
				file = new File("D:/error.html");
				is = new FileInputStream(file);
			} catch (FileNotFoundException e1) {
				LOG.error("无法返回error页面：页面文件不存在");
			}
			return new Response(responseHead, file.length(), is);
		}
	}

	/**
	 * 将目录中的内容写入到html页面中，并将该html以字节数组的形式返回。
	 * 
	 * @param file
	 */
	private byte[] expandDirectory(File file) {
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("展开目录失败！试图展开一个非目录文件");
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
			LOG.error("无法展开目录：不支持的编码格式");
		}
		return bytes;
	}

	/**
	 * 根据key查找配置文件获取Http应答头的信息，并转换为字节数组返回。
	 * 
	 * @param key
	 * @return
	 */
	private byte[] getResponseHead(String key) {
		byte[] bytes = null;
		try {
			bytes = map.get(key).getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("无法获取Http应答头:不支持的编码格式");
		}
		return bytes;
	}
}
