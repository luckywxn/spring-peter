package cn.strongculture.service;

import cn.strongculture.*;

@Component("userService")
//@Scope("prototype")
public class UserServiceImpl implements InitializingBean,UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;
//    @Override
//    public void setBeanName(String beanName) {
//        this.beanName = beanName;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }


    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
    }

}
