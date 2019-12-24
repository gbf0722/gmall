package com.atguigu.gmall.pms;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {
    @Autowired
    private AmqpTemplate amqpTemplate;

    private String EXCHANGE_NAME = "qwer";
    @Test
    void contextLoads() {
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME, "item.aa" , "aaa");
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gmall-search-queue222", durable = "true"),
            exchange = @Exchange(value = "qwer", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key ={"item.aa"}
    ))
    public void listener(String msg) {
        System.out.println("接受到的信息为："+msg);

    }
}
