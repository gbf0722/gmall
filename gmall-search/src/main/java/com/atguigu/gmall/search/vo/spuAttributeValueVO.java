package com.atguigu.gmall.search.vo;

import lombok.Data;

@Data
public class spuAttributeValueVO {
    private Long id;  //商品和属性关联的数据表的主键Id；
    private Long prodectAttributeId;    //当前sku对应的属性的attr_id
    private String name;    //属性名 比如电池
    private String value;   //属性的值
    private Long spuId;     //这个属性关系对应的spu的id；
}
