package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsFeign extends GmallWmsApi {
}
