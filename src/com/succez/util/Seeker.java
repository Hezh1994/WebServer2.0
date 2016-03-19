package com.succez.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.succez.exception.IsNotDirectory;

/**
 * ����Http�����е�·�������ļ��Ĺ����ࡣ������ļ����򷵻�File����;����Ǽ��ϣ��򷵻�һ��File�����List����
 * 
 * @author succez
 *
 */
public class Seeker {

	/**
	 * ����url���Ҳ�����ָ���ļ�����:url =
	 * "/d/document/test.txt",���ܷ���·��ΪD:\document\test.txt���ļ���
	 * 
	 * @param url
	 * @return
	 * @throws FileNotFoundException
	 *             �ļ�������ʱ���׳����쳣��
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
			throw new FileNotFoundException("�ļ�������");
		}
		return file;
	}

	/**
	 * ������ǰĿ¼������һ��File�����List���ϣ������е�ÿ��Ԫ�ض�Ӧ��ǰĿ¼�µõ�ÿ���ļ���Ŀ¼��
	 * 
	 * @param file
	 *            Ŀ¼
	 * @return
	 * @throws IsNotDirectory
	 *             �����ݹ����Ĳ�������һ��Ŀ¼�������ļ�ʱ���׳����쳣��
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
