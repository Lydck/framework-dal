package com.taiping.framework.dal.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanWrapper;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.taiping.framework.dal.parser.SqlBean.Entry;
import com.taiping.framework.dal.util.ParamMapUtil;
import com.taiping.framework.dal.parser.SqlParser;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanMap;

/**
 * 翻页处理规则 原来的find方法ORMapping没有依赖Column注解里的配置,是采用Spring JDBC
 * Template默认的Mapping机制，导致java属性与数据表字段不匹配的情况发生 现通过自定义的rowmapper来实现
 * 
 * @author
 * @param <T> 泛型T
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class DefaultBeanPropertyRowMapper<T> implements RowMapper<T> {
	/** The class we are mapping to */
	private Class<T> mappedClass;

	/** Whether we're strictly validating */
	private boolean checkFullyPopulated;

	/** Whether we're defaulting primitives when mapping a null value */
	private boolean primitivesDefaultedForNullValue;

	/** Map of the fields with ColumnName by entity class annotation */
	private Map<String, String> mappedFields;

	/**
	 * Create a new BeanPropertyRowMapper for bean-style configuration.
	 * 
	 * @see #setMappedClass
	 * @see #setCheckFullyPopulated
	 */
	public DefaultBeanPropertyRowMapper() {
	}

	/**
	 * Create a new BeanPropertyRowMapper, accepting unpopulated properties in the
	 * target bean.
	 * <p>
	 * Consider using the {@link #newInstance} factory method instead, which allows
	 * for specifying the mapped type once only.
	 * 
	 * @param mappedClass the class that each row should be mapped to
	 */
	public DefaultBeanPropertyRowMapper(Class<T> mappedClass) {
		initialize(mappedClass);
	}

	/**
	 * Create a new BeanPropertyRowMapper.
	 * 
	 * @param mappedClass         the class that each row should be mapped to
	 * @param checkFullyPopulated whether we're strictly validating that all bean
	 *                            properties have been mapped from corresponding
	 *                            database fields
	 */
	public DefaultBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
		initialize(mappedClass);
		this.checkFullyPopulated = checkFullyPopulated;
	}

	/**
	 * Set the class that each row should be mapped to.
	 * 
	 * @param mappedClass the mapped class.
	 */
	public void setMappedClass(Class<T> mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		} else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to "
						+ mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}

	/**
	 * Initialize the mapping metadata for the given class.
	 * 
	 * @param mappedClass the mapped class.
	 */
	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new ConcurrentHashMap<>();
		Entry[] entries = SqlParser.genSqlEntries(mappedClass);
		for(Entry e : entries) {
			mappedFields.putIfAbsent(e.columnName, e.propName);
		}
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case. Any upper
	 * case letters are converted to lower case with a preceding underscore.
	 * Ope Map: XXX_XX -> xxxXx
	 * @param name the string containing original name
	 * @return the converted name
	 */
	private String underscoreName(String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		String[] split = name.split("_");
		if(split.length == 0) {
			return name;
		}
		result.append(split[0].toLowerCase());
		for(int i = 1; i < split.length; i++) {
			result.append(split[i].charAt(0));
			result.append(split[i].substring(1).toLowerCase());
		}
		if (log.isDebugEnabled()) {
			log.debug("Underscore Table ColumnName:" + name +" To property name: " + result);
		}
		return result.toString();
	}

	/**
	 * Get the class that we are mapping to.
	 * 
	 * @return class 映射类
	 */
	public final Class<T> getMappedClass() {
		return this.mappedClass;
	}

	/**
	 * Set whether we're strictly validating that all bean properties have been
	 * mapped from corresponding database fields.
	 * <p>
	 * Default is {@code false}, accepting unpopulated properties in the target
	 * bean.
	 * 
	 * @param checkFullyPopulated Return whether we're strictly validating that all
	 *                            bean properties have been mapped from
	 *                            corresponding database fields.
	 */
	public void setCheckFullyPopulated(boolean checkFullyPopulated) {
		this.checkFullyPopulated = checkFullyPopulated;
	}

	/**
	 * Return whether we're strictly validating that all bean properties have been
	 * mapped from corresponding database fields.
	 * 
	 * @return 判断结果
	 */
	public boolean isCheckFullyPopulated() {
		return this.checkFullyPopulated;
	}

	/**
	 * Set whether we're defaulting Java primitives in the case of mapping a null
	 * value from corresponding database fields.
	 * <p>
	 * Default is {@code false}, throwing an exception when nulls are mapped to Java
	 * primitives.
	 * 
	 * @param primitivesDefaultedForNullValue Return whether we're defaulting Java
	 *                                        primitives in the case of mapping a
	 *                                        null value from corresponding database
	 *                                        fields.
	 */
	public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
		this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
	}

	/**
	 * Return whether we're defaulting Java primitives in the case of mapping a null
	 * value from corresponding database fields.
	 * 
	 * @return 判断结果
	 */
	public boolean isPrimitivesDefaultedForNullValue() {
		return primitivesDefaultedForNullValue;
	}

	/**
	 * Extract the values for all columns in the current row.
	 * <p>
	 * Utilizes public setters and result set metadata.
	 * 
	 * @param rs        结果集
	 * @param rowNumber 行数
	 * @return 结果值
	 * @exception SQLException SQL异常
	 * @see java.sql.ResultSetMetaData
	 */
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		T t;
		try {
			t = mappedClass.newInstance();
		} catch (Exception e2) {
			log.info("Initialize the given " + mappedClass + "failed!");
			throw new SQLException(e2.getMessage());
		}
		BeanMap create = ParamMapUtil.getBeanMap(t);
		create.setBean(t);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			// get property name
			String property = mappedFields.get(column);
			if(property == null) {
				property = underscoreName(column);
				mappedFields.put(column, property);
			}
			Class<?> propertyType = create.getPropertyType(property);
			if(propertyType == null) {
				log.warn("Entity Class was not offered property for column:" + column);
				continue;
			}
			if (log.isDebugEnabled()) {
                log.debug("Mapping column '" + column + "' to property '" + property + "' of type " + propertyType.getName());
            }
			Object value = JdbcUtils.getResultSetValue(rs, index, propertyType);
			create.put(property, value);
		}
		return t;
	}

	/**
	 * Initialize the given BeanWrapper to be used for row mapping. To be called for
	 * each row.
	 * <p>
	 * The default implementation is empty. Can be overridden in subclasses.
	 * 
	 * @param bw the BeanWrapper to initialize
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
	}

	/**
	 * Static factory method to create a new BeanPropertyRowMapper (with the mapped
	 * class specified only once).
	 * 
	 * @param mappedClass the class that each row should be mapped to
	 * @param <T>         泛型对象
	 * @return newInstances
	 */
	public static <T> BeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
		BeanPropertyRowMapper<T> newInstance = new BeanPropertyRowMapper<T>();
		newInstance.setMappedClass(mappedClass);
		return newInstance;
	}

}
