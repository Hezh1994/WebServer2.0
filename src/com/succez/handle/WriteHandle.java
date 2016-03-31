package com.succez.handle;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteHandle implements Runnable {
	private static final Logger LOG = LoggerFactory
			.getLogger(WriteHandle.class);
	private SelectionKey key;

	public WriteHandle(SelectionKey key) {
		super();
		this.key = key;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		SocketChannel socketChannel = (SocketChannel) key.channel();
		FileInputStream is = (FileInputStream) key.attachment();
		FileChannel channel = is.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		buffer.clear();
		try {
			int count = channel.read(buffer);
			if (count > 0) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					try {
						socketChannel.write(buffer);
					} catch (IOException e) {
						LOG.info("客户端取消了下载");
						is.close();
						channel.close();
						socketChannel.close();
						break;
					}
				}
			} else {
				is.close();
				channel.close();
				socketChannel.close();
			}
		} catch (IOException e) {
			LOG.error("写入信息失败：发生I/O错误");
		}

	}
}
