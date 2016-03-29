package com.succez.appliction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.AppOutputStream;
import com.succez.util.ConfigReader;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 展开目录，提供文件下载的应用。
 * 
 * @author succez
 *
 */
public class HttpAppliction implements Appliction {
	private static final Logger LOG = LoggerFactory
			.getLogger(HttpAppliction.class);
	private Map<String, String> map;
	String responseHead;

	public HttpAppliction() {
		ConfigReader reader = ConfigReader.getConfigReader();
		this.map = reader.getMap();
	}

	/**
	 * 处理浏览器请求的方法
	 * 
	 * @throws IOException
	 */
	@Override
	public void service(Request request, Response response)
			throws CanNotHandleException, IOException {
		LOG.info("应用程序处理请求");
		if (!map.get("requestType").equals(request.getRequestType())) {
			throw new CanNotHandleException("无法处理的请求类型");
		}
		AppOutputStream outputStream = response.getOutputStream();
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// 访问的是一个目录，展开改目录
				LOG.info("展开目录");
				responseHead = map.get("directoryHead");
				expandDirectory(outputStream, request, file);
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// 图片类型的文件，预览图片
					LOG.info("预览图片");
					responseHead = map.get("image");
					try {
						outputStream.write(responseHead);
						outputStream.write(file);
					} catch (IOException e) {
						LOG.error("预览图片失败:发生I/O错误");
					}

				} else {
					// 下载文件
					LOG.info("下载文件");
					responseHead = map.get("fileHead");
					try {
						outputStream.write(responseHead);
						outputStream.write(file);
					} catch (IOException e) {
						LOG.error("下载文件失败:发生I/O错误");
					}
				}
			}
		} catch (FileNotFoundException e) {
			responseHead = map.get("notFound");
			File file = null;
			try {
				file = new File("D:/error.html");
				outputStream.write(responseHead);
				outputStream.write(file);
			} catch (FileNotFoundException e1) {
				LOG.error("无法返回error页面：页面文件不存在");
			}
		}
	}

	/**
	 * 展开目录
	 * 
	 * @param outputStream
	 * @param file
	 */
	private void expandDirectory(AppOutputStream outputStream, Request request,
			File file) {
		try {
			outputStream.write(responseHead);
			List<File> files = null;
			try {
				files = Seeker.getFiles(file);
			} catch (IsNotDirectory e) {
				LOG.error("展开目录失败:试图展开一个非目录文件");
			}
			outputStream.write("<html>");
			outputStream.write("<head>");
			outputStream.write("<title>");
			outputStream.write(request.getUrl());
			outputStream.write("</title>");
			outputStream.write("</head>");
			outputStream.write("<body>");
			outputStream.write("<p style=\"color:blue\">当前路径"
					+ request.getUrl() + "下的内容为:</p>");
			for (File f : files) {
				outputStream.write("<a href=\"http://localhost:"
						+ request.getPort() + request.getUrl() + "/"
						+ f.getName() + "\">");
				outputStream.write(f.getName() + "</a>");
				outputStream.write("<br />");
			}
			outputStream.write("</body>");
			outputStream.write("</html>");
		} catch (IOException e) {
			LOG.error("展开目录失败:发生I/O错误");
		}
	}
}
