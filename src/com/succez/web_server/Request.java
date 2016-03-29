package com.succez.web_server;

/**
 * 封装了部分http请求信息的类，包括请求类型requestType、请求资源路径url等。提供get、set方法。
 * 
 * @author succez
 *
 */
public class Request {
	private String requestType;
	private String url;
	private String RANGE;
	private int port;

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
		this.RANGE = rang;
	}

	/**
	 * 获取http请求头的rang属性
	 * 
	 * @return
	 */
	public String getRang() {
		return RANGE;
	}

	public void setRang(String rang) {
		this.RANGE = rang;
	}

	/**
	 * 返回请求类型，如GET POSt
	 * 
	 * @return 请求类型
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * 返回请求的资源路径，形如/d/document
	 * 
	 * @return 请求资源路径
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
		result = prime * result + ((RANGE == null) ? 0 : RANGE.hashCode());
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
		if (RANGE == null) {
			if (other.RANGE != null)
				return false;
		} else if (!RANGE.equals(other.RANGE))
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
