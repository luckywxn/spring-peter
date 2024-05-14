package cn.strongculture;

public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
