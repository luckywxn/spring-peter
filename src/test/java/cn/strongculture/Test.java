package cn.strongculture;

import cn.strongculture.service.UserService;

public class Test {
    public static void main(String[] args) {
        PeterApplicationContext context = new PeterApplicationContext(AppConfig.class);

//        System.out.println(context.getBean("userService"));
//        System.out.println(context.getBean("userService"));
//        System.out.println(context.getBean("userService"));

        UserService userService = (UserService)context.getBean("userService");
        userService.test(); //1.代理对象 2.业务test

    }
}
