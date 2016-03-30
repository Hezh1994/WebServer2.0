package com.succez.appliction;

import java.io.IOException;

import com.succez.exception.CanNotHandleException;
import com.succez.web_server.Request;
import com.succez.web_server.Response;

/**
 * 定义了所有运行在服务器上的程序都必须实现的方法。
 * 
 * @author succez
 *
 */
public interface Appliction {
	/**
	 * 处理服务器传递过来的客户端request请求，response用于将应答返回给客户端
	 * 
	 * @param request
	 *            请求信息
	 * @param response
	 *            用于返回应答信息
	 * @throws IOException
	 *             无法将应答信息写入客户端时，抛出该异常
	 */
	public void service(Request request, Response response)
			throws CanNotHandleException, IOException;
}
