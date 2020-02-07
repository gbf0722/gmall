package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.api.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 11:52:58
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    int closeOrder(String orderToken);

    public int payOrder(String orderToken);
}
