package com.taiping.framework.dal.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taiping.framework.dal.constant.DbType;
import com.taiping.framework.dal.entity.UserEntity;
import com.taiping.framework.dal.entity.anno.Column;
import com.taiping.framework.dal.entity.anno.ID;
import com.taiping.framework.dal.entity.anno.Table;
import com.taiping.framework.dal.exception.DalException;
import com.taiping.framework.dal.parser.SqlBean.Entry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlParser<T> {
	
	private static final Map<Class<?>, SqlHolder> cache = new ConcurrentHashMap<>();
	
	public static <T> SqlHolder getSqlHolder(T t) {
		Class<? extends Object> entityClass = t.getClass();
		SqlHolder result;
		// 未缓存命中
		if((result = cache.get(entityClass)) == null) {
			result = genBaseSql(t);
			cache.put(entityClass, result);
		}
		return result;
	}

	public static <T> SqlHolder genBaseSql(T t) throws DalException {
		Table table = t.getClass().getAnnotation(Table.class);
		if(table == null) {
			log.error("Entity:" + t.getClass() + "was not Annotated by @Table!");
			throw new DalException("dal.001:Entity was not annoted by @Table");
		}
		// 获取表名
		String tableName = table.value();
		// 定义CRUD语句
		StringBuffer insert = new StringBuffer("INSERT INTO " + tableName + "(");
		StringBuffer delete = new StringBuffer("DELETE FROM " + tableName + " WHERE ");
		StringBuffer update = new StringBuffer("UPDATE " + tableName + " SET ");
		StringBuffer select = new StringBuffer("SELECT ");
		// 数据项，第0个为主键
		Entry[] entries = genSqlEntries(t.getClass());
		// 返回结果
		SqlHolder holder = new SqlHolder();
		// 根据数据库类型生成数据列
		StringBuffer columns = new StringBuffer();
		StringBuffer params = new StringBuffer();
		for(int i = 1; i < entries.length - 1; i++) {
			columns.append(entries[i].columnName + ", ");
			params.append(":" + entries[i].propName + ", ");
			update.append(entries[i].columnName + " = :" + entries[i].propName + ", ");
			select.append(entries[i].columnName + ", ");
		}
		// 最后一个
		columns.append(entries[entries.length - 1].columnName);
		params.append(":" + entries[entries.length - 1].propName);
		update.append(entries[entries.length - 1].columnName + " = :" + entries[entries.length - 1].propName);
		select.insert(7, entries[0].columnName + ", ");
		select.append(entries[entries.length - 1].columnName + " FROM " + tableName + " WHERE " + entries[0].columnName + " = :" + entries[0].propName);
		DbType dbType = DbType.ORACLE;
		switch(dbType) {
		// Oracle数据库，自增主键使用的是sequence
		case ORACLE:
			columns.insert(0, entries[0].columnName + ", ");
			params.insert(0, entries[0].sequenceName + ".nextval, ");
			break;
		// MySQL数据库，自增主键不用提供
		case MYSQL:
			break;
		default:
			break;
		}
		insert.append(columns).append(") VALUES(").append(params).append(")");
		SqlBean sqlBean = new SqlBean(insert.toString(), entries);
		holder.setInsertSqlBean(sqlBean);
		// delete 主键为条件
		delete.append(entries[0].columnName).append(" = :").append(entries[0].propName);
		sqlBean = new SqlBean(delete.toString(), entries);
		holder.setDeleteSqlBean(sqlBean);
		// update 主键为条件
		update.append(" WHERE " + entries[0].columnName + " = :" + entries[0].propName);
		sqlBean = new SqlBean(update.toString(), entries);
		holder.setUpdateSqlBean(sqlBean);
		// select 主键为条件
		sqlBean = new SqlBean(select.toString(), entries);
		holder.setSelectSqlBean(sqlBean);
		holder.setEntries(entries);
		return holder;
	}
	
	public static <T> Entry[] genSqlEntries(Class<T> tClazz) {
		Field[] declaredFields = tClazz.getDeclaredFields();
		List<Entry> entry = new ArrayList<>();
		int i = 1;
		for(Field field : declaredFields) {
			if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
				// 有final或者static修饰，跳过
				continue;
			}
			ID id = field.getAnnotation(ID.class);
			String name = field.getName();
			// 自增主键
			if(id != null) {
				String key = id.value();
				DbType dbType = DbType.ORACLE;
				switch(dbType) {
				// Oracle数据库，自增主键使用的是sequence
				case ORACLE:
					String sequence = id.sequence();
					entry.add(0, new Entry(key, name, sequence));
					break;
				case MYSQL:
					entry.add(0, new Entry(key, name));
				default:
					
				}
				continue;
			}
			// column列处理
			Column column = field.getAnnotation(Column.class);
			String columnName;
			// 没有用column标记，则列名为属性名
			if(column == null) {
				columnName = name;
			} else {
				columnName = column.value();
			}
			entry.add(i++, new Entry(columnName, name));
		}
		return entry.stream().toArray(Entry[]::new);
	}
	
	
	public static void main(String[] args) throws DalException {
		UserEntity e = new UserEntity();
		e.setUserId(2);
		e.setUserName("lydck");
		SqlHolder genBaseSql = genBaseSql(e);
		log.info("{}", genBaseSql);
	}
	
}

