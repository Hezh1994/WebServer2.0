package com.succez.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ��ȡ�����ļ��Ĺ�����
 * 
 * @author biu
 *
 */
public class ConfigReader {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);
	private static ConfigReader configReader = null;
	private static Map<String, String> configMap = new HashMap<String, String>();

	/**
	 * һ�ν������ļ�����Ϣȫ����ȡ���������HashMap�С�
	 */
	private ConfigReader() {
		Properties properties = new Properties();
		InputStream is = null;
		try {
			is = Writer.class.getResourceAsStream("/httpResponse.properties");
			properties.load(is);
			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = properties.getProperty(key);
				configMap.put(key, value);
			}
		} catch (Exception e) {
			LOG.error("��ȡ�����ļ�ʧ��");
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {

			}
		}
	}

	/**
	 * ����ģʽ
	 * 
	 * @return ConfigReader
	 */
	public static synchronized ConfigReader getConfigReader() {
		if (configReader == null) {
			configReader = new ConfigReader();
		}
		return configReader;
	}

	public Map<String, String> getMap() {
		return configMap;
	}

}
