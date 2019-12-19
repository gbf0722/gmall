package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;
import vo.SaleVO;


import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVO {
    private Long skuId;  //商品Id；
    private String title;  //标题
    private String defaultImage; //图片
    private BigDecimal price; //加入购物车时的价格
    private Integer count;   //数量
    private Boolean store;
    private List<SkuSaleAttrValueEntity> skuAttrValue;  // 商品规格参数
    private List<SaleVO> sales;
    private BigDecimal weight;
}