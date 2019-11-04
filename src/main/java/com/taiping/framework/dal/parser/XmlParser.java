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

import com.taiping.framework.dal.constant.DbType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**解析项目中sqlMap下的
 * @author xiangyj
 *
 */
@Slf4j
public class XmlParser {
	
	private static final Map<String, SqlDbType> sqls = new ConcurrentHashMap<>();
	
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
				String dbType = root.attribute("dbType").getText();
				DbType type = DbType.getEnum(dbType);
				// 当前默认为Oracle
				if(type == null) {
					type = DbType.ORACLE;
				}
				@SuppressWarnings("unchecked")
				List<Element> elements = root.elements();
				for(Element e : elements) {
					String id = e.attributeValue("id");
					String sqlID = nameSpace + "." + id;
					SqlDbType sqlDbType = new SqlDbType(sqlID, e.getTextTrim(), type);
					sqls.putIfAbsent(sqlID, sqlDbType);
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
		return sqls.get(sqlId).orgSql;
	}
	
	public static DbType getDbType(String sqlId) {
		return sqls.get(sqlId).dbType;
	}
	
	@Setter
	@Getter
	private static class SqlDbType {
		
		private String sqlId;
		
		private String orgSql;
		
		private DbType dbType;

		public SqlDbType(String sqlId, String orgSql, DbType dbType) {
			super();
			this.sqlId = sqlId;
			this.orgSql = orgSql;
			this.dbType = dbType;
		}
	}

}
