package com.atguigu.gmall.order.feign;

import feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsFeign extends GmallSmsApi {
}
