package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVO extends SkuInfoEntity {





    // 积分活动  gmall-sms表
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    // 满减活动  sms_sku_full_reduction表
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //sms_sku_ladder表  满几件  打几折
    private Integer fullCount;
    private BigDecimal discount;

    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer ladderAddOther;

    //pms_sku_sale_attr_value表
    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<String> images;

}
