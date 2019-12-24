package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.api.entity.OrderEntity;
import com.atguigu.gmall.oms.api.entity.OrderItemEntity;
import com.atguigu.gmall.oms.api.vo.OrderItemVO;
import com.atguigu.gmall.oms.api.vo.OrderSubmitVO;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.feign.GmallPmsFeign;
import com.atguigu.gmall.oms.feign.GmallUmsFeign;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private GmallPmsFeign gmallPmsClient;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GmallUmsFeign gmallUmsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVO orderSubmitVO) {
        //保存orderEntity
        MemberReceiveAddressEntity address = orderSubmitVO.getAddress();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverRegion(address.getProvince());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverCity(address.getCity());

        /*Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberById(orderSubmitVO.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
        orderEntity.setMemberId(memberEntity.getId());*/

        //清算每个商品赠送积分
        orderEntity.setIntegration(0);
        orderEntity.setGrowth(0);

        orderEntity.setDeleteStatus(0);
        orderEntity.setStatus(0);

        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setDeliveryCompany(orderSubmitVO.getDeliveryCompany());
        orderEntity.setSourceType(1);
        orderEntity.setPayType(orderSubmitVO.getPayType());
        orderEntity.setTotalAmount(orderSubmitVO.getTotalPrice());
        orderEntity.setOrderSn(orderSubmitVO.getOrderToken());
        this.save(orderEntity);
        Long orderId = orderEntity.getId();

        //保存订单详情 orderItemEntity
        List<OrderItemVO> items = orderSubmitVO.getItems();
        items.forEach(item -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setSkuId(item.getSkuId());

            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();

            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();

            itemEntity.setSkuPrice(skuInfoEntity.getPrice());
            itemEntity.setSkuAttrsVals(JSON.toJSONString(item.getSkuAttrValue()));
            itemEntity.setCategoryId(skuInfoEntity.getCatalogId());
            itemEntity.setOrderId(orderId);
            itemEntity.setOrderSn(orderSubmitVO.getOrderToken());
            itemEntity.setSpuId(skuInfoEntity.getSpuId());
            itemEntity.setSkuName(skuInfoEntity.getSkuName());
            itemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
            itemEntity.setSkuQuantity(item.getCount());
            itemEntity.setSpuName(spuInfoEntity.getSpuName());
            this.orderItemDao.insert(itemEntity);

            //int i =1 / 0 ;
        });
        //在创建订单之后，在响应之前发送延时消息，达到定时关单的效果
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "order.ttl", orderSubmitVO.getOrderToken());


        return orderEntity;
    }

}