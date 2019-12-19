package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVO {
    private long skuId;
    private Integer count;
    private Long wareSkuId;  //锁定库存的Id；
    private Boolean lock;  //锁定状态

}
