package com.taiping.framework.dal.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import lombok.extern.slf4j.Slf4j;

/**解析项目中sqlMap下的
 * @author xiangyj
 *
 */
@Slf4j
public class XmlParser {
	
	private static final Map<String, String> sqls = new ConcurrentHashMap<String, String>();
	
	static {
		log.debug("Initializing sqlMap Resources...");
		// 获取
		File[] listFiles = new File("sqlMap/").listFiles();
		if(listFiles == null || listFiles.length == 0) {
			String error = "Initializing SqlMap Resources Error! Besause resources folder 'sqlMap' was not Provided";
			log.error(error);
			throw new RuntimeException(error);
		}
		BufferedInputStream bis = null;
		for(File f : listFiles) {
			try {
				bis = new BufferedInputStream(new FileInputStream(f));
				int available = bis.available();
				byte[] by = new byte[available];
				bis.read(by);
				String xml = new String(by);
				Document xmlDoc = DocumentHelper.parseText(xml);
				Element root = xmlDoc.getRootElement();
				String nameSpace = root.attribute("namespace").getText();
				@SuppressWarnings("unchecked")
				List<Element> elements = root.elements();
				for(Element e : elements) {
					String id = e.attributeValue("id");
					sqls.put(nameSpace + "." + id, e.getTextTrim());
				}
			} catch (Exception e) {
				log.error("Initializing sqlMap Resources error:", e);
				throw new RuntimeException(e.getMessage());
			} finally {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static String getOrgSql(String sqlId) {
		return sqls.get(sqlId);
	}

}
