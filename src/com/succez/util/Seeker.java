package com.succez.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.succez.exception.IsNotDirectory;

/**
 * 根据http请求中的路径查找文件的工具类。如果是文件，则返回File对象；如果是集合，则返回List集合。
 * 
 * @author succez
 *
 */
public class Seeker {
	/**
	 * 根据url查找并返回指定文件。如url = "/d/document/test.txt"，则返回路径D:\document\test.txt的文件
	 * 
	 * @param url
	 * @return
	 * @throws FileNotFoundException
	 *             文件不存在时，抛出该异常。
	 */
	public static File getFile(String url) throws FileNotFoundException {
		String str = url.substring(1);
		String filePath;
		if (str.indexOf("/") == -1) {
			filePath = str + ":/";
		} else {
			filePath = str.replaceFirst("/", ":/");
		}
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("文件不存在");
		}
		return file;
	}

	/**
	 * 遍历当前目录，返回一个包含File对象的List集合，集合中的每个元素对应当前目录下的每个文件或目录
	 * 
	 * @param file
	 *            目录
	 * @return
	 * @throws IsNotDirectory
	 *             当传递过来的参数不是一个目录，而是文件时，抛出该异常
	 */
	public static List<File> getFiles(File file) throws IsNotDirectory {
		if (!file.isDirectory()) {
			throw new IsNotDirectory();
		}
		File[] files = file.listFiles();
		List<File> fileList = new ArrayList<File>();
		for (File f : files) {
			fileList.add(f);
		}
		return fileList;
	}
}
