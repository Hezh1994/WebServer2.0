package com.succez.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.web_server.Request;

/**
 * ��������ͨ����̬����parse�Ի������е�http������н������õ�һ��Request����
 * 
 * @author succez
 *
 */
public class Parser {
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

	/**
	 * �Ի������е�HttpRequest���н���������һ��Request����
	 * 
	 * @param request
	 *            �������е�������Ϣ
	 * @return Request����
	 * @throws IOException
	 *             ����IO���󣬶�ȡʧ�ܡ�
	 */
	public static Request parse(String requestInfo) throws IOException {
		LOG.info("����������Ϣ");
		BufferedReader reader = new BufferedReader(
				new StringReader(requestInfo));
		String firstLine = reader.readLine();
		String requestType = firstLine.substring(0, firstLine.indexOf(" "));
		String url = firstLine.substring(firstLine.indexOf(" ") + 1,
				firstLine.indexOf("HTTP") - 1);
		return new Request(requestType, url);
	}
}
