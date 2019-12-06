    package com.atguigu.gmall.pms.dto;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
    @ToString
    @Data
    public class SkuSaleDTO {
        private Long skuId;

        // 积分营销相关字段
        private BigDecimal growBounds;
        private BigDecimal buyBounds;
        private List<Integer> work;


        // 打折相关的字段
        private Integer fullCount;
        private BigDecimal discount;
        private Integer ladderAddOther;

        // 满减相关的字段
        private BigDecimal fullPrice;
        private BigDecimal reducePrice;
        private Integer fullAddOther;
    }
