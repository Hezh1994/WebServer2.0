package com.succez.handle;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 对SelectionKey进行处理的接口，分别对三种不同的状态acceptable、readable、writable进行处理。
 * 
 * @author succez
 *
 */
public interface Handle {

	// 当通道isAcceptable时，对通道进行处理
	public void handleAccept(SelectionKey key) throws IOException;

	// 当通道isReadable时，对通道进行的处理
	public void handleRead(SelectionKey key) throws IOException;

	// 当通道isWritable时，对通道进行的处理
	public void handleWrite(SelectionKey key) throws IOException;
}
