package com.atguigu.gmall.wms;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {


    //根据skuId查寻库存信息
    @GetMapping("wms/waresku/{skuId}")
    public Resp<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId")Long skuId);
}
