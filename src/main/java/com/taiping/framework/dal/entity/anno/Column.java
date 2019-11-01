package com.taiping.framework.dal.entity.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	/** Column列名*/
	String value() default "";
	
	/** 数据类型*/
	Class<?> type() default String.class;
}
