package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.Orderexception;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.oms.api.vo.OrderItemVO;
import com.atguigu.gmall.oms.api.vo.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import entity.MemberEntity;
import entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Autowired
    private GmallCartFeign gmallCartFeign;
    @Autowired
    private GmallOmsFeign gmallOmsFeign;

    @Autowired
    private GmallUmsFeign gmallUmsFeign;
    @Autowired
    private GmallWmsFeign gmallWmsFeign;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX = "order:token:";


    public OrderConfirmVO confirm() {
        OrderConfirmVO confirmVO = new OrderConfirmVO();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        if (userId == null) {
            return null;
        }
        // 获取用户的收货地址列表， 根据用户id查询收货地址列表
        Resp<List<MemberReceiveAddressEntity>> addressResp = this.gmallUmsFeign.queryAddressesByUserId(userId);
        List<MemberReceiveAddressEntity> addressentities = addressResp.getData();
        confirmVO.setAddresses(addressentities);

        //获取购物车中的选中的商品信息  skuId count
        Resp<List<Cart>> cartResp = this.gmallCartFeign.queryCheckedCartsByUserId(userId);
        List<Cart> carts = cartResp.getData();
        if (CollectionUtils.isEmpty(carts)) {
            throw new Orderexception("请勾选购物车商品");
        }

        //查询sku的信息
        List<OrderItemVO> itemVOS = carts.stream().map(cart -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            Long skuId = cart.getSkuId();
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                orderItemVO.setWeight(skuInfoEntity.getWeight());
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                orderItemVO.setPrice(skuInfoEntity.getPrice());
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                orderItemVO.setSkuId(skuId);
                orderItemVO.setCount(cart.getCount());
            }

            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsFeign.querySkuSaleAttrValueBySkuId(skuId);
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = saleAttrValueResp.getData();
            orderItemVO.setSkuAttrValue(skuSaleAttrValueEntities);

            Resp<List<WareSkuEntity>> wareResp = this.gmallWmsFeign.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
                    return wareSkuEntity.getStock() > 0;
                }));
            }
            return orderItemVO;
        }).collect(Collectors.toList());
        confirmVO.setOrderItems(itemVOS);

        //查询用户的信息
        Resp<MemberEntity> memberEntityResp = this.gmallUmsFeign.queryMemberById(userId);
        MemberEntity memberEntity = memberEntityResp.getData();
        confirmVO.setBounds(memberEntity.getIntegration());

        //生成唯一的标志，防止重复提交（响应到页面有一份，有一份保存到redis中）
        String orderToken = IdWorker.getIdStr();
        confirmVO.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(TOKEN_PREFIX+orderToken,orderToken);

        return confirmVO;
    }

    public void submit(OrderSubmitVO orderSubmitVO) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //1.防止重复提交，查询redis中没有orderToken的信息，有，则是第一次提交，放行并删除redis中的orderToken

        //使用鲁啊脚本防止提交
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVO.getOrderToken();
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0) {
            throw new Orderexception("订单不可重复提交");
        }


        //2.校验价格，总价一致就行

        //获取送货清单
        List<OrderItemVO> items = orderSubmitVO.getItems();
        BigDecimal totalPrice = orderSubmitVO.getTotalPrice();
        if (CollectionUtils.isEmpty(items)) {
            throw new Orderexception("没有购买的商品，请到购物中勾选商品");
        }
        //获取实时的总价信息
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                BigDecimal price = skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
                return price;
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        //判断从数据库中的查的实时价格与页面价格是否一致
        if (currentTotalPrice.compareTo(totalPrice) !=0) {
            throw new Orderexception("页面已过期，请重新刷新后下单");
        }



        //3.校验库存是否充足，并锁定库存，一次性提示所有库存不够的信息
        List<SkuLockVO> lockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> wareResp = this.gmallWmsFeign.checkAndLockStock(lockVOS);
        if (wareResp.getCode() != 0) {
            throw new Orderexception(wareResp.getMsg());
        }

        //int i =1/0;
        //4.下单（创建订单及订单详情）
       try {
            orderSubmitVO.setUserId(userInfo.getId());
            this.gmallOmsFeign.saveOrder(orderSubmitVO);
        } catch (Exception e) {
            e.printStackTrace();
            //发送消息给wms ,解锁对应的库存
            this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "stock.unlock", orderToken);
            throw new Orderexception("服务器错误，创建订单失败");
        }


        //5.删除购物车(发送消息删除购物车)
       HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getId());
        List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
        map.put("skuIds", skuIds);
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","cart.delete",map);

    }


}
