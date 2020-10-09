package com.yh.hand.write.demo.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 元胡
 * @date 2020/10/09 1:44 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.ANNOTATION_TYPE,ElementType.METHOD,ElementType.CONSTRUCTOR,ElementType.PARAMETER})
public @interface AutoWired {
}