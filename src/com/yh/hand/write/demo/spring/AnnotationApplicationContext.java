package com.yh.hand.write.demo.spring;

import com.yh.hand.write.demo.exception.NotFundClassException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 元胡
 * @since 2020/10/09 12:20 下午
 */
public class AnnotationApplicationContext implements ApplicationContext {
    private Class config;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonInstanceMap = new ConcurrentHashMap<>();

    public AnnotationApplicationContext(Class config) {
        this.config = config;

        // 只加载非原型（单列）的，非懒加载的类
        this.scanAndCreateBean();

        // 依赖注入
        initAndDi();
    }

    // 依赖注入
    private void initAndDi() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            // 懒加载不创建
            if (beanDefinition.getLazy()) {
                return;
            }

            // 原型不处理
            if (beanDefinition.getScope().equals(ScopeEnum.prototype)) {
                return;
            }

            // 创建bean
            this.doCreateBean(beanDefinition);
        }
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        try {
            Object instance;
            // 优先从单例对象查询 可能存在在Di时就已经把蛋类对象放入池中了
            if (beanDefinition.getScope().equals(ScopeEnum.singleton)
                && (instance = singletonInstanceMap.get(beanDefinition.getBeanName())) != null) {
                return instance;
            }

            // bean实例
            instance = beanDefinition.getClazz().getDeclaredConstructor().newInstance();
            // bean Class类
            Class beanClass = beanDefinition.getClazz();
            // 依赖注入
            this.di(instance, beanClass);
            if (beanDefinition.getScope().equals(ScopeEnum.singleton)) {
                // 投入单例对象池
                singletonInstanceMap.put(beanDefinition.getBeanName(), instance);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 依赖注入
    private void di(Object instance, Class beanClass) {
        Field[] fields = beanClass.getFields();
        if (fields.length == 0) {
            return;
        }

        // 遍历属性查找需要注入的类 进行依赖注入
        for (Field field : fields) {
            if (!field.isAnnotationPresent(AutoWired.class)) {
                continue;
            }
            String beanName = field.getName();
            Object diInstance;
            // 先从单例对象查询需要注入的bean对象
            if ((diInstance = singletonInstanceMap.get(beanName)) != null) {
                try {
                    field.set(instance, diInstance);
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            // 单例对象池中没有则创建bean对象
            BeanDefinition beanDefinition;
            if ((beanDefinition = this.beanDefinitionMap.get(beanName)) == null) {
                throw new NotFundClassException(
                    "Not fund class error ! Di bean:" + beanDefinition.getClazz().getName());
            }
            if (!beanDefinition.getScope().equals(ScopeEnum.prototype)) {
                diInstance = this.doCreateBean(beanDefinition);
            }
            try {
                field.set(instance, diInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private void scanAndCreateBean() {
        // 是否使用了ComponentScan注解
        if (!config.isAnnotationPresent(ComponentScan.class)) {
            return;
        }

        List<Class> beanClassFromResource = this.getBeanClassFromResource();
        for (Class beanClass : beanClassFromResource) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setClazz(beanClass);
            // component注解
            Component component = (Component)beanClass.getAnnotation(Component.class);
            // 注解的bean名称
            String beanName = component.value();
            beanDefinition.setBeanName(beanName);

            // 原型、单列判断
            if (beanClass.isAnnotationPresent(Scope.class)) {
                Scope scopeAnnotation = (Scope)beanClass.getAnnotation(Scope.class);
                ScopeEnum value = scopeAnnotation.value();
                if (value.equals(ScopeEnum.prototype)) {
                    beanDefinition.setScope(ScopeEnum.prototype);
                    beanDefinitionMap.put(beanName, beanDefinition);
                    continue;
                }
                beanDefinition.setScope(ScopeEnum.singleton);
            }
            // 是否懒加载判断
            if (beanClass.isAnnotationPresent(Lazy.class)) {
                beanDefinition.setLazy(Boolean.TRUE);
                beanDefinitionMap.put(beanName, beanDefinition);
                continue;
            }

            // 缓存bean定义map
            beanDefinitionMap.put(beanName, beanDefinition);
        }

    }

    private List<Class> getBeanClassFromResource() {
        List<Class> loadClasses = new ArrayList<>();
        // 获得注解
        ComponentScan componentScanAnnotation = (ComponentScan)config.getAnnotation(ComponentScan.class);
        // 获得注解值扫描包
        String scanPackage = componentScanAnnotation.value();
        // 类加载器
        ClassLoader classLoader = config.getClassLoader();
        // 类加载的是文件 是绝对路径
        scanPackage = scanPackage.replace(".", "/");
        // 获得资源信息
        URL resource = classLoader.getResource(scanPackage);
        // 文件夹
        File file = new File(resource.getFile());
        this.loadClassFromResource(file, loadClasses, classLoader);
        return loadClasses;
    }

    private void loadClassFromResource(File file, List<Class> loadClasses, ClassLoader classLoader) {
        // 遍历文件
        for (File listFile : file.listFiles()) {
            if (listFile.isFile()) {
                // 全路径名文件名
                String fileName = listFile.getAbsolutePath();
                // 是否是class文件
                if (!fileName.endsWith(".class")) {
                    continue;
                }

                // 类名
                String className = fileName.substring(fileName.indexOf("com"), fileName.lastIndexOf(".class"));
                className.replace("\\", ".");

                // 加载类 在Spring中实际使用的是ASM技术判断是否有什么注解
                Class beanClass = null;
                try {
                    beanClass = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                // 没有使用Component说明不需要Spring管理
                if (!beanClass.isAnnotationPresent(Component.class)) {
                    continue;
                }
                loadClasses.add(beanClass);
            }
            this.loadClassFromResource(listFile, loadClasses, classLoader);
        }
    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition;
        Object instance;
        // 是否有bean定义文件
        if ((beanDefinition = beanDefinitionMap.get(beanName)) == null) {
            return null;
        }

        // 优先考虑单列对象池中是否有
        if (beanDefinition.getScope().equals(ScopeEnum.singleton)
            && (instance = singletonInstanceMap.get(beanName)) != null) {
            return instance;
        }

        // 原型的每次都生成
        if (beanDefinition.getScope().equals(ScopeEnum.prototype)) {
            return this.doCreateBean(beanDefinition);
        }

        // 懒加载，第一次使用才加载类到singletonMap
        if (beanDefinition.getLazy()) {
            return this.doCreateBean(beanDefinition);
        }
        return null;
    }
}
