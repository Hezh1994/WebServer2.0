package com.succez.handle;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * ��SelectionKey���д���Ľӿڣ��ֱ�����ֲ�ͬ��״̬acceptable��readable��writable���д���
 * 
 * @author succez
 *
 */
public interface Handle {

	// ��ͨ��isAcceptableʱ����ͨ�����д���
	public void handleAccept(SelectionKey key) throws IOException;

	// ��ͨ��isReadableʱ����ͨ�����еĴ���
	public void handleRead(SelectionKey key) throws IOException;

	// ��ͨ��isWritableʱ����ͨ�����еĴ���
	public void handleWrite(SelectionKey key) throws IOException;
}
