package com.atguigu.gmall.order.vo;

import entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private String orderToken; //防止重复
    private MemberReceiveAddressEntity address;
    private Integer payType;
    private String deliveryCompany;  //配送方式
    private List<OrderItemVO> items;
    private Integer bounds;
    private BigDecimal totalPrice;  //校验价格
}
