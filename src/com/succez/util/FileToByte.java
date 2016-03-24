package com.succez.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.succez.exception.CanNotTranslateException;

/**
 * 将文件转换为字节数组的类
 * 
 * @author succez
 *
 */
public class FileToByte {
	private static final Logger LOG = LoggerFactory.getLogger(FileToByte.class);

	/**
	 * 将指定文件转换为找一个字节数组并返回，当文件过大时将无法进行转换
	 * 
	 * @param File
	 * @return byte[]
	 * @throws CanNotTranslateException
	 *             ,IOException
	 * @throws IOException
	 */
	public static byte[] fileToByte(File file) throws CanNotTranslateException,
			IOException {
		FileInputStream fis = null;
		try {
			long fileLength = file.length();
			if (fileLength > Integer.MAX_VALUE) {
				throw new CanNotTranslateException("文件太大，无法进行转换");
			}
			fis = new FileInputStream(file);
			byte[] buffer = new byte[(int) fileLength];
			int off = 0;
			int numRead = 0;
			while (off < buffer.length
					&& (numRead = fis.read(buffer, off, buffer.length - off)) >= 0) {
				off += numRead;
			}
			if (off != fileLength) {
				throw new IOException();
			}
			return buffer;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
				LOG.error("输入流关闭失败");
			}
		}
	}
}
