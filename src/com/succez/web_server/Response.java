package com.succez.web_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.ConfigReader;
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
	public void write(SocketChannel socketChannel) throws CanNotHandleException {
		LOG.info("��Ӧhttp����");
		ConfigReader reader = ConfigReader.getConfigReader();
		Map<String, String> map = reader.getMap();
		if (!(map.get("requestType")).equals(request.getRequestType())) {
			throw new CanNotHandleException("�޷��������������");
		}
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// �ͻ��˷���Ŀ¼ʱ��չ����Ŀ¼��
				expandDirectory(socketChannel, file);
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// ͼƬ���͵��ļ���Ԥ��ͼƬ
					showPicture(socketChannel, file);
				} else {
					// �ͻ��˷����ļ�ʱ�����ظ��ļ�
					downloadFile(socketChannel, file);
				}
			}
		} catch (FileNotFoundException e) {
			// �ļ�������ʱ������404 NotFound
			try {
				notFound(socketChannel);
			} catch (IOException e1) {
				LOG.error("�ļ�����ʧ��");
			}
		}
	}

	/**
	 * ���ͻ����������Դ����ΪͼƬ���͵��ļ�ʱ���ṩͼƬ��Ԥ������
	 * 
	 * @param socketChannel
	 * @param file
	 */
	private void showPicture(SocketChannel socketChannel, File file) {
		// TODO Auto-generated method stub
		try {
			Writer.writeHeadToChannel("directoryHead", socketChannel);
			Writer.writeToChannel(file, socketChannel);
		} catch (IOException e) {
			LOG.error("Ԥ��ͼƬʧ��");
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
	private void downloadFile(SocketChannel socketChannel, File file) {
		try {
			Writer.writeHeadToChannel("fileHead", socketChannel);
			Writer.writeToChannel(file, socketChannel);
		} catch (IOException e) {

			LOG.error("�����ļ�ʧ��!�޷�������д����ͨ����");
		}
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
	private void expandDirectory(SocketChannel socketChannel, File file) {
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("չ��Ŀ¼ʧ��!��ͼչ��һ����Ŀ¼�ļ�");
		}
		try {
			Writer.writeHeadToChannel("directoryHead", socketChannel);
		} catch (IOException e) {
			LOG.error("չ��Ŀ¼ʧ��!�޷�������д����ͨ����");
		}
		StringBuilder sb = new StringBuilder(500);
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
			sb.append("<a href=\"http://localhost:"
					+ socketChannel.socket().getLocalPort() + request.getUrl()
					+ "/" + f.getName() + "\">");
			sb.append(f.getName() + "</a>");
			sb.append("</br>");
		}
		sb.append("</body>");
		sb.append("</html>");
		try {
			Writer.writeToChannel(sb.toString(), socketChannel);
		} catch (IOException e) {
			LOG.error("չ��Ŀ¼ʧ��!�޷�������д����ͨ����");
		}
	}

	/**
	 * ���ͻ����������Դ������ʱ�����ش�ҳ�档
	 * 
	 * @param socketChannel
	 *            ͨ��
	 * @throws IOException
	 */
	private void notFound(SocketChannel socketChannel) throws IOException {
		Writer.writeHeadToChannel("notFound", socketChannel);
		StringBuilder sb = new StringBuilder(500);
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<h1>");
		sb.append("404 Not Found");
		sb.append("</h1>");
		sb.append("<h3>");
		sb.append("�ܱ�Ǹ,���ʵ���Դ������!������ַ�Ƿ���ȷ.");
		sb.append("</h3>");
		sb.append("</body>");
		sb.append("</html>");
		Writer.writeToChannel(sb.toString(), socketChannel);
	}
}
