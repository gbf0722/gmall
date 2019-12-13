package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsVO {
    private Long id ;       //skuId
    private Long brandId;   //品牌Id
    private String brandName;  //品牌名
    private Long productCategoryId;  //sku的分类Id；
    private String prodectCategoryName;  //sku的名字


    private String pic;     //sku的默认图片
    private String name;    //这是需要检索的sku的标题
    private BigDecimal price;     //sku的价格
    private Integer sale;       //sku的销量
    private Long stock;     //sku的库存
    private  Integer sort;      //排序分，热度
    //保存当前的sku的所有的需要检索的属性
    //检索属性来源于spu的基本属性中的search_type=1(销售属性都已经拼接在标题中了)
    private List<spuAttributeValueVO> attributeValueVoList;     //检索属性









}
