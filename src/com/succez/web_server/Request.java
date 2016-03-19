package com.succez.web_server;

/**
 * 封装了部分http请求信息的类，包括请求类型requestType、请求资源路径url。 提供requestType的url的get、set方法。
 * 
 * @author succez
 *
 */
public class Request {
	private String requestType;
	private String url;

	public Request() {

	}

	public Request(String requestType, String url) {
		super();
		this.requestType = requestType;
		this.url = url;
	}

	/**
	 * 返回请求的类型，如GET POST
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
