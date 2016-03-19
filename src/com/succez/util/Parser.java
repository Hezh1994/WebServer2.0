package com.succez.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.succez.web_server.Request;

/**
 * 解析器，通过静态方法parse对缓冲区中的http请求进行解析，得到一个Request对象。
 * 
 * @author succez
 *
 */
public class Parser {

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
		BufferedReader reader = new BufferedReader(
				new StringReader(requestInfo));
		String firstLine = reader.readLine();
		String[] strArray = firstLine.split(" ");// 有问题，当请求的资源路径中有空格的时候
		String requestType = strArray[0];
		String url = strArray[1];
		return new Request(requestType, url);
	}
}
