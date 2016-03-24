package com.succez.handle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotHandleException;
import com.succez.exception.CanNotTranslateException;
import com.succez.exception.IsNotDirectory;
import com.succez.util.ConfigReader;
import com.succez.util.FileToByte;
import com.succez.util.Seeker;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * ����Request���õ�һ��Response��
 * 
 * @author succez
 *
 */
public class RequestHandler {
	private static final Logger LOG = LoggerFactory
			.getLogger(RequestHandler.class);
	private Request request;
	private SocketChannel socketChannel;
	private String encoding;
	private Map<String, String> map;

	/**
	 * ��ȡ���÷ּ����õ�map���ϣ����ñ����ʽ��
	 * 
	 * @param request
	 * @param socketChannel
	 */
	public RequestHandler(Request request, SocketChannel socketChannel) {
		super();
		this.request = request;
		this.socketChannel = socketChannel;
		ConfigReader reader = ConfigReader.getConfigReader();
		this.map = reader.getMap();
		this.encoding = map.get("encoding");
	}

	/**
	 * ����ͻ������󣬻�ȡ������Դ���ֽ����飬��HttpӦ��ͷ���������Դ��װ��Response�С�
	 * 
	 * @param socketChannel
	 *            �ͻ���ͨ��
	 * @return Response
	 * @throws CanNotHandleException
	 *             ���������޷�����ʱ���׳����쳣
	 */
	public Response processRequest(SocketChannel socketChannel)
			throws CanNotHandleException {
		LOG.info("��������");
		if (!(map.get("requestType")).equals(request.getRequestType())) {
			throw new CanNotHandleException("�޷��������������");
		}
		byte[] bytes = null;
		String responseHead;
		try {
			File file = Seeker.getFile(request.getUrl());
			if (file.isDirectory()) {
				// �ͻ��˷���Ŀ¼ʱ��չ����Ŀ¼��
				responseHead = map.get("directoryHead");
				bytes = expandDirectory(file);
				Response response = new Response(responseHead, bytes);
				return response;
			} else {
				String s = file.getName();
				String suf = s.substring(s.indexOf(".") + 1, s.length());
				String imageType = map.get("imageType");
				if (imageType.contains(suf)) {
					// ͼƬ���͵��ļ���Ԥ��ͼƬ
					responseHead = map.get("directoryHead");
					try {
						bytes = FileToByte.fileToByte(file);
					} catch (Exception e) {
						LOG.error("�޷�Ԥ��" + file.getName() + ":��ȡ�ļ�ʧ��");
					}
					return new Response(responseHead, bytes);

				} else {
					Response response = null;
					// �ͻ��˷����ļ�ʱ�����ظ��ļ�
					try {
						response = downloadFile(file);
					} catch (Exception e) {
						LOG.error("�����ļ�" + file.getName() + "ʧ��:�޷���ȡ�ļ�");
					}
					return response;
				}
			}
		} catch (FileNotFoundException e) {
			// �ļ�������ʱ������404 NotFound
			responseHead = map.get("notFound");
			bytes = returnNotFound();
			return new Response(responseHead, bytes);
		}
	}

	private Response downloadFile(File file) throws CanNotTranslateException,
			IOException {
		if (request.getRang() == null) {
			// ȫ������
			String responseHead = map.get("fileHead");
			byte[] bytes = FileToByte.fileToByte(file);
			return new Response(responseHead, bytes);
		} else {
			// �ϵ�����
			LOG.info("��ʼ�ϵ�����");

			return null;
		}
	}

	/**
	 * �ͻ����������Դ������ʱ�����ظ�ҳ�档
	 * 
	 * @return
	 */
	private byte[] returnNotFound() {
		StringBuilder sb = new StringBuilder(500);
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append(request.getUrl());
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<p>");
		sb.append("404 Not Found");
		sb.append("</p>");
		sb.append("<p>");
		sb.append("�ܱ�Ǹ,���ʵ���Դ������!������ַ�Ƿ���ȷ.");
		sb.append("</p>");
		sb.append("</body>");
		sb.append("</html>");
		byte[] bytes = null;
		try {
			bytes = sb.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("�޷�����404:��֧�ֵı�������");
		}
		return bytes;
	}

	/**
	 * ��Ŀ¼�е�����д�뵽htmlҳ���У�������html���ֽ�������ʽ���ء�
	 * 
	 * @param file
	 */
	private byte[] expandDirectory(File file) {
		List<File> files = null;
		try {
			files = Seeker.getFiles(file);
		} catch (IsNotDirectory e) {
			LOG.error("չ��Ŀ¼ʧ��!��ͼչ��һ����Ŀ¼�ļ�");
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
			sb.append("<br />");
		}
		sb.append("</body>");
		sb.append("</html>");
		byte[] bytes = null;
		try {
			bytes = sb.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("�޷�չ��Ŀ¼:��֧�ֵı�������");
		}
		return bytes;
	}
}
