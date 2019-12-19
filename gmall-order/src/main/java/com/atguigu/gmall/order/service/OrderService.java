package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.Orderexception;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.order.feign.GmallCartFeign;
import com.atguigu.gmall.order.feign.GmallPmsFeign;
import com.atguigu.gmall.order.feign.GmallUmsFeign;
import com.atguigu.gmall.order.feign.GmallWmsFeign;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.OrderItemVO;
import com.atguigu.gmall.order.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import entity.MemberEntity;
import entity.MemberReceiveAddressEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private GmallUmsFeign gmallUmsFeign;
    @Autowired
    private GmallWmsFeign gmallWmsFeign;

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
        Resp<MemberEntity> memberEntityResp = this.gmallUmsFeign.userInfo(userId);
        MemberEntity memberEntity = memberEntityResp.getData();
        confirmVO.setBounds(memberEntity.getIntegration());

        //生成唯一的标志，防止重复提交（响应到页面有一份，有一份保存到redis中）
        String orderToken = IdWorker.getIdStr();
        confirmVO.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(TOKEN_PREFIX+orderToken,orderToken);

        return confirmVO;
    }

    public void submit(OrderSubmitVO orderSubmitVO) {

        //1.防止重复提交，查询redis中没有orderToken的信息，有，则是第一次提交，放行并删除redis中的orderToken


        //2.校验价格，总价一致就行

        //3.校验库存是否充足，并锁定库存，一次性提示所有库存不够的信息


        //4.下单（创建订单及订单详情）



        //5.删除购物车



    }
}
