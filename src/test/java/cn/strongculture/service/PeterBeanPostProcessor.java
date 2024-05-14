package cn.strongculture.service;

import cn.strongculture.BeanPostProcessor;
import cn.strongculture.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class PeterBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("postProcessBeforeInitialization");
        if (bean instanceof UserServiceImpl){
            ((UserServiceImpl)bean).test();
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("postProcessAfterInitialization");
        if (bean instanceof UserServiceImpl){
            Object proxy = Proxy.newProxyInstance(PeterBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    return method.invoke(bean, args);
                }
            });
            return proxy;
        }
        return bean;
    }
}
