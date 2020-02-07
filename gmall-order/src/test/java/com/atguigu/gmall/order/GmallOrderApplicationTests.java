package com.atguigu.gmall.order;

import com.atguigu.gmall.order.pay.AlipayTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallOrderApplicationTests {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Test
    void contextLoads() {
        System.out.println(alipayTemplate);
    }

}
