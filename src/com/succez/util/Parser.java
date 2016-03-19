package com.succez.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.succez.web_server.Request;

/**
 * ��������ͨ����̬����parse�Ի������е�http������н������õ�һ��Request����
 * 
 * @author succez
 *
 */
public class Parser {

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
		BufferedReader reader = new BufferedReader(
				new StringReader(requestInfo));
		String firstLine = reader.readLine();
		String[] strArray = firstLine.split(" ");// �����⣬���������Դ·�����пո��ʱ��
		String requestType = strArray[0];
		String url = strArray[1];
		return new Request(requestType, url);
	}
}
