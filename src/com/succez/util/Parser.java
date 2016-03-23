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
	 * @param request
	 *            缓冲区中的请求信息
	 * @return Request对象
	 * @throws IOException
	 *             发生IO错误，读取失败。
	 */
	public static Request parse(String requestInfo) throws IOException {
		LOG.info("解析请求信息");
		BufferedReader reader = new BufferedReader(
				new StringReader(requestInfo));
		String firstLine = reader.readLine();
		String requestType = firstLine.substring(0, firstLine.indexOf(" "));
		String url = firstLine.substring(firstLine.indexOf(" ") + 1,
				firstLine.indexOf("HTTP") - 1);
		return new Request(requestType, url);
	}
}
