package com.succez.handle;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取附加在key上的输入流，将请求的资源写入通道中。
 * 
 * @author succez
 *
 */
public class WriteHandle implements Runnable {
	private static final Logger LOG = LoggerFactory
			.getLogger(WriteHandle.class);
	private SelectionKey key;

	public WriteHandle(SelectionKey key) {
		super();
		this.key = key;
	}

	/**
	 * 获取文件输入流得到文件通道，将通道的数据写入字节缓冲区中，将字节缓冲区中的内容写给客户端。
	 */
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
						/**
						 * 客户端取消下载后，则通道关闭，若继续向通道中执行写操作，则抛出I/O异常。
						 */
						LOG.info("客户端取消了下载");
						closeAll(socketChannel, is, channel);
						break;
					}
				}
			} else {
				closeAll(socketChannel, is, channel);
			}
		} catch (IOException e) {
			LOG.error("写入信息失败：发生I/O错误");
		}

	}

	/**
	 * 关闭输入流、文件通道、客户端通道
	 * 
	 * @param socketChannel
	 * @param is
	 * @param channel
	 * @throws IOException
	 */
	private void closeAll(SocketChannel socketChannel, FileInputStream is,
			FileChannel channel) throws IOException {
		is.close();
		channel.close();
		socketChannel.close();
	}
}
