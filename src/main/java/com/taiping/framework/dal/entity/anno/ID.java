package com.taiping.framework.dal.entity.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {

	/** 主键名称，不指定的话，默认为被标记字段的属性名称*/
	String value();
	
	/**Oracle数据递增序列名称
	 * @return
	 */
	String sequence() default "";
}
