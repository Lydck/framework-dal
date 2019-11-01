package com.taiping.framework.dal.parser;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.taiping.framework.dal.exception.DalException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FreeMarkerParser {

	public static String processTemplate(String freSql, Object root) throws DalException {
		Writer writer = new StringWriter();
		try {
			Template t = new Template("sqlId", new StringReader(freSql), new Configuration(new Version("2.3.28")));
			t.process(root, writer);
		} catch (Exception e) {
			log.error("Parse Freemarker Template SQL Error:", e);
			throw new DalException(e.getMessage());
		}
		return writer.toString();
	}
	
}
