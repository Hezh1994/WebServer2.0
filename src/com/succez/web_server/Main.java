package com.succez.web_server;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		Server server;
		try {
			server = new Server();
			try {
				server.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
