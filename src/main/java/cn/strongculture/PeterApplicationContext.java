package cn.strongculture;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PeterApplicationContext {
    private Class configClass;
    private ConcurrentMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    public PeterApplicationContext(Class configClass) {
        this.configClass = configClass;

        //解析配置类
        //ComponentScan注解 --> 扫描路径 --> 扫描 --> BeanDefinition --> beanDefinitionMap
        scan(configClass);

        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            //依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(instance,bean);
                }
            }
            //Aware回调
            if (instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            //BeanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

            return instance;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        System.out.println(path);
        //扫描
        //Bootstrap --->jre/lib
        //Ext --->jre/ext
        //App --->classpath
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("cn/strongculture/service");
        File file = new File(resource.getFile());
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                String absolutePath = f.getAbsolutePath();
                if (absolutePath.endsWith(".class")){
                    String className = absolutePath.substring(absolutePath.indexOf("cn"), absolutePath.indexOf(".class"));
                    className = className.replace("\\", ".");

                    System.out.println(className);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)){
                            //表示当前这个类是一个Bean
                            //解析类 -->BeanDefinition

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(beanPostProcessor);
                            }


                            Component component = clazz.getAnnotation(Component.class);
                            String beanName = component.value();
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)){
                                Scope scope = clazz.getAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());
                            }else {
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName, beanDefinition);

                            //放入map中
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                if (singletonObjects.containsKey(beanName)){
                    return singletonObjects.get(beanName);
                }else {
                    //创建Bean对象
                    return createBean(beanName, beanDefinition);
                }
            }else {
                //创建Bean对象
                return createBean(beanName,beanDefinition);
            }
        }else {
            //不存在对应的Bean
            throw new NullPointerException();
        }
    }

}
