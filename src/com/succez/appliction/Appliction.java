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
	public void service(Request request, Response response)
			throws CanNotHandleException, IOException;
}
