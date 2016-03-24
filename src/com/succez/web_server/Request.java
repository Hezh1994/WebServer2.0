package com.succez.web_server;

/**
 * ��װ�˲���http������Ϣ���࣬������������requestType��������Դ·��url�� �ṩrequestType��url��get��set������
 * 
 * @author succez
 *
 */
public class Request {
	private String requestType;
	private String url;
	private String rang;

	public Request() {

	}

	public Request(String requestType, String url) {
		super();
		this.requestType = requestType;
		this.url = url;
	}

	public Request(String requestType, String url, String rang) {
		super();
		this.requestType = requestType;
		this.url = url;
		this.rang = rang;
	}

	/**
	 * ��ȡHttp����ͷ��rang����
	 * 
	 * @return
	 */
	public String getRang() {
		return rang;
	}

	public void setRang(String rang) {
		this.rang = rang;
	}

	/**
	 * ������������ͣ���GET POST
	 * 
	 * @return ��������
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * �����������Դ·��������/d/document
	 * 
	 * @return ������Դ·��
	 */
	public String getUrl() {
		return url;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rang == null) ? 0 : rang.hashCode());
		result = prime * result
				+ ((requestType == null) ? 0 : requestType.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (rang == null) {
			if (other.rang != null)
				return false;
		} else if (!rang.equals(other.rang))
			return false;
		if (requestType == null) {
			if (other.requestType != null)
				return false;
		} else if (!requestType.equals(other.requestType))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
