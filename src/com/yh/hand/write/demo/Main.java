package com.yh.hand.write.demo;

import com.yh.hand.write.demo.spring.AnnotationApplicationContext;
import com.yh.hand.write.demo.spring.ApplicationContext;

/**
 * @author 元胡
 * @date 2020/10/09 12:01 下午
 */
public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationApplicationContext(AppConfig.class);
        applicationContext.getBean("userService");
    }
}
