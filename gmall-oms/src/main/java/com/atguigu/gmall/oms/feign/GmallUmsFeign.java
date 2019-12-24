package com.atguigu.gmall.oms.feign;

import feign.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsFeign extends GmallUmsApi {
}
