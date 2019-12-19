package com.atguigu.gmall.order.feign;

import feign.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsFeign extends GmallUmsApi {
}
