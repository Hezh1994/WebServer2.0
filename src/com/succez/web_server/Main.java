package com.succez.web_server;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static Server server;

	/**
	 * 程序入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.info("初始化并启动服务器");
		try {
			server = new Server();
		} catch (IOException e) {
			LOG.error("服务器初始化失败");
		}
		server.start();
		Main main = new Main();
		Main.CommandListener listener = main.new CommandListener();
		listener.start();
	}

	/**
	 * 内部类，用来监听控制台输入的命令，当监听到控制台输入exit时，释放资源，退出程序。
	 * 
	 * @author succez
	 *
	 */
	class CommandListener extends Thread {
		public void run() {
			Scanner scan = new Scanner(System.in);
			String command;
			while (true) {
				command = scan.nextLine();
				if ("exit".equals(command)) {
					server.setFlag(false);
					try {
						server.shutDown();
					} catch (IOException e) {
						LOG.error("服务器无法正常关闭");
						System.exit(1);
					}
					scan.close();
					System.exit(0);
				}
			}
		}
	}
}
