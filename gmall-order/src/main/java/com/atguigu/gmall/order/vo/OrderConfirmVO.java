package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.api.vo.OrderItemVO;
import entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVO {





    //收获地址  ums_member_receive_address表
    private List<MemberReceiveAddressEntity> addresses;

    //购物清单，根据购物车页面传过来的skuIds查询
    private List<OrderItemVO> orderItems;

    //可用积分
    private Integer bounds;

    //订单令牌，防止重复提交
    private String orderToken;

}
