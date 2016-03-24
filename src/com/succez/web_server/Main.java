package com.succez.web_server;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static Server server;

	/**
	 * �������
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.info("��ʼ��������������");
		try {
			server = new Server();
		} catch (IOException e) {
			LOG.error("��������ʼ��ʧ��");
		}
		server.start();
		Main main = new Main();
		Main.CommandListener listener = main.new CommandListener();
		listener.start();
	}

	/**
	 * �ڲ��࣬������������̨��������������������̨����exitʱ���ͷ���Դ���˳�����
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
						LOG.error("�������޷������ر�");
						System.exit(1);
					}
					scan.close();
					System.exit(0);
				}
			}
		}
	}
}
