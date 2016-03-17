package com.succez.util;

import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Process {
	private static final Logger LOG = LoggerFactory.getLogger(Process.class);

	/**
	 * 对在选择器中注册过的通道所对应的键进行处理.主要判断键所对应的通道的状态,判断通道是acceptable、readable还是writable.
	 * 在通道不同的状态分别进行读、写操作.
	 * 
	 * @param key
	 *            通道所对应的选择键
	 */
	public static void process(SelectionKey key) {

	}
}
