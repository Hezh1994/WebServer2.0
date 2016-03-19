package com.succez.web_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.Seeker;
import com.succez.util.Writer;

/**
 * ��Ӧ�ͻ��˵�http���󣬽��ͻ����������Դд�뵽ͨ���С�
 * 
 * @author succez
 *
 */
public class Response {
	private static final Logger LOG = LoggerFactory.getLogger(Response.class);
	private Request request;

	public Response(Request request) {
		super();
		this.request = request;
	}

	/**
	 * ��Ӧ�ͻ��˵�Http���󣬽��ͻ������������Դд�뵽ͨ���У�Ŀǰֻ�ܴ���GET�������ͻ����������Դ������һ��Ŀ¼��
	 * �򷵻�һ��չʾ��Ŀ¼�������ļ����ļ��е�html��
	 * ���ͻ�����������ļ����ͣ�������ظ��ļ������ͻ����������ͼƬ���͵��ļ������������Ԥ����ͼƬ��
	 * 
	 * @param socketChannel
	 *            �ͻ���ͨ��
	 * @throws CanNotHandleException
	 *             �����޷�����ʱ���׳����쳣
	 */
	public void response(SocketChannel socketChannel)
			throws CanNotHandleException {
		LOG.info("��Ӧhttp����");
		if (!"GET".equals(request.getRequestType())) {
			throw new CanNotHandleException("�޷��������������");
		}
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				try {
					expandDirectory(socketChannel, file);
				} catch (IOException e) {
					LOG.error("�޷������ݴӻ�����д�뵽ͨ����");
				} catch (IsNotDirectory e) {
					LOG.error("��ͼչ��һ����Ŀ¼�ļ�");
				}

			} else {
				try {
					downloadFile(socketChannel, file);
				} catch (IOException e) {
					LOG.error("�޷������ݴӻ�����д�뵽ͨ����");
				}
			}
		} catch (FileNotFoundException e) {
		}
	}

	/**
	 * ���ͻ����������Դʱһ���ļ�ʱ�����ṩ���ء���httpӦ��ͷ���ļ����ֽ������װ��ByteBuffer�У����ӻ�����д�뵽ͨ���С�
	 * 
	 * @param socketChannel
	 *            �ͻ���ͨ��
	 * @param file
	 *            ������ļ�
	 * @param buffer
	 *            ������
	 * @throws IOException
	 *             ����IO����ʱ���޷������ݴӻ�����д��ͨ����ʱ���׳����쳣
	 */
	private void downloadFile(SocketChannel socketChannel, File file)
			throws IOException {
		Properties properties = new Properties();
		InputStream is = this.getClass().getResourceAsStream(
				"httpResponse.properties");
		properties.load(is);
		is.close();
		String fileHead = properties.getProperty("fileHead");
		Writer.writeToChannel(fileHead, socketChannel);
		Writer.writeToChannel(file, socketChannel);
	}

	/**
	 * ���ͻ����������Դ��һ��Ŀ¼ʱ����չ����Ŀ¼��չʾ��Ŀ¼�������ļ�����html����ʽ���ظ��ͻ��ˡ�
	 * 
	 * @param socketChannel
	 * @param file
	 * @param buffer
	 * @throws IOException
	 * @throws IsNotDirectory
	 */
	private void expandDirectory(SocketChannel socketChannel, File file)
			throws IOException, IsNotDirectory {
		List<File> files = Seeker.getFiles(file);
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\nConnection:keep-alive\r\nServer:WebServer\r\n\r\n");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<p style=\"color:blue\">��ǰ·��" + request.getUrl()
				+ "�µ�����Ϊ:</p>");
		for (File f : files) {
			sb.append("<a href=\"http://192.168.13.157:"
					+ socketChannel.socket().getLocalPort() + request.getUrl()
					+ "/" + f.getName() + "\">");
			sb.append(f.getName() + "</a>");
			sb.append("</br>");
		}
		sb.append("</body>");
		sb.append("</html>");
		socketChannel.write(ByteBuffer.wrap(sb.toString().getBytes("utf-8")));
	}
}
