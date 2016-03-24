package com.succez.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.web_server.Request;

/**
 * 解析器，通过静态方法parse对缓冲区中的http请求进行解析，得到一个Request对象。
 * 
 * @author succez
 *
 */
public class Parser {
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

	/**
	 * 对缓冲区中的HttpRequest进行解析，返回一个Request对象。
	 * 
	 * @param requestInfo
	 *            缓冲区中的请求信息
	 * @return
	 * @throws IOException
	 *             发生I/O错误，读取失败。
	 */
	public static Request parse(String requestInfo) throws IOException {
		LOG.info("解析请求信息");
		BufferedReader reader = new BufferedReader(
				new StringReader(requestInfo));
		// 解析第一行
		String line = reader.readLine();
		String requestType = line.substring(0, line.indexOf(" "));
		String url = line.substring(line.indexOf(" ") + 1,
				line.indexOf("HTTP") - 1);
		Request request = new Request(requestType, url);
		String rang = null;
		// 解析Rang
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("Rang")) {
				rang = line.substring(line.indexOf(":") + 1);
			}
		}
		request.setRang(rang);
		return request;
	}
}
