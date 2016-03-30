package com.succez.appliction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.IsNotDirectory;
import com.succez.util.AppOutputStream;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 部署在服务器上用于展开目录的应用，类似于一个servlet。
 * 
 * @author succez
 *
 */
public class ShowDirectory implements Appliction {
	private static final Logger LOG = LoggerFactory
			.getLogger(ShowDirectory.class);

	/**
	 * 展开客户端访问的目录。
	 */
	public void service(Request request, Response response) throws IOException {
		AppOutputStream os = response.getOutputStream();
		File file = Seeker.getFile(request.getUrl());
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("展开目录失败:试图展开一个非目录文件");
		}
		SimpleDateFormat dateFormater = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		os.write("<html>");
		os.write("<head>");
		os.write("<title>");
		os.write(request.getUrl());
		os.write("</title>");
		os.write("</head>");
		os.write("<body>");
		os.write("<p style=\"color:red\">当前时间为" + dateFormater.format(date)
				+ "</p>");
		os.write("<p style=\"color:red\">当前路径" + request.getUrl()
				+ "下的内容为:</p>");
		for (File f : files) {
			os.write("<a href=\"http://localhost:" + request.getPort()
					+ request.getUrl() + "/" + f.getName() + "\">");
			os.write(f.getName() + "</a>");
			os.write("<br />");
		}
		os.write("</body>");
		os.write("</html>");
	}
}
