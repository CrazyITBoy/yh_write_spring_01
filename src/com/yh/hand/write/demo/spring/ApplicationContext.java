package com.yh.hand.write.demo.spring;

/**
 * @author 元胡
 * @date 2020/10/09 12:00 下午
 */
public interface ApplicationContext {

    /**
     * 根据bean名称获取bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
