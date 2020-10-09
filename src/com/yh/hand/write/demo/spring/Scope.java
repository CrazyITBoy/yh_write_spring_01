package com.yh.hand.write.demo.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 元胡
 * @date 2020/10/09 12:54 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Scope {
    ScopeEnum value() default ScopeEnum.singleton;
}
