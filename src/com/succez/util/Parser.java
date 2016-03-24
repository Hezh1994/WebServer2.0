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
		// ������һ��
		String line = reader.readLine();
		String requestType = line.substring(0, line.indexOf(" "));
		String url = line.substring(line.indexOf(" ") + 1,
				line.indexOf("HTTP") - 1);
		Request request = new Request(requestType, url);
		String rang = null;
		// ����Rang
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("Rang")) {
				rang = line.substring(line.indexOf(":") + 1);
			}
		}
		request.setRang(rang);
		return request;
	}
}
