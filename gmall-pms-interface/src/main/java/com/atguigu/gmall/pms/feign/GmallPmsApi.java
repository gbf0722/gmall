package com.atguigu.gmall.pms.feign;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface GmallPmsApi {
    //品牌详情查询
    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> brandInfo(@PathVariable("brandId") Long brandId);

    //分类详情查询
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> catInfo(@PathVariable("catId") Long catId);

    //查找指定的spu下的所有的sku
    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);


    //分页查询已发布的spu的信息
    @PostMapping("pms/spuinfo/{status}")
    public Resp<List<SpuInfoEntity>> querySpuInfoByStatus(@RequestBody QueryCondition condition, @PathVariable("status") Integer status);

    //根据spuId查询检索属性和值
    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> querySearchAttrValue(@PathVariable("spuId")Long spuId);

    //根据分页查询spu所有的spu的信息
    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition);
}
