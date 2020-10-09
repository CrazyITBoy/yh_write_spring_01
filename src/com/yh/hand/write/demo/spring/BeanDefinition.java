package com.yh.hand.write.demo.spring;

/**
 * @author 元胡
 * @date 2020/10/09 1:02 下午
 */
public class BeanDefinition {

    private Class clazz;

    private String beanName;

    private ScopeEnum scope;

    private Boolean lazy=false;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }


    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }
}
