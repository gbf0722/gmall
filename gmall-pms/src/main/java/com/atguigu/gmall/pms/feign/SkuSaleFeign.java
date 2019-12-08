package com.atguigu.gmall.pms.feign;

import feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface SkuSaleFeign extends GmallSmsApi {
}
