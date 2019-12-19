package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsFeign;
import com.atguigu.gmall.cart.feign.GmallSmsFeign;
import com.atguigu.gmall.cart.feign.GmallWmsFeign;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vo.SaleVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {


    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private GmallWmsFeign gmallWmsFeign;
    @Autowired
    private GmallSmsFeign gmallSmsFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;
    public static final String KEY_PREFIX = "gmall:cart:";
    public static final String PRICE_PREFIX = "gmall:sku";



    private String getLoginState() {
        String key = KEY_PREFIX;
        // 获取登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println(userInfo);
        if (userInfo.getId() != null) {
            key += userInfo.getId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    public void addCart(Cart cart) {
        String key = getLoginState();

        //获取购物车，获取的是用户的hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();
        //判断购物车中是否有该记录
        if (hashOps.hasKey(skuId)) {
            // 有  则更新数量 获取购物车的sku的记录
            String cartJson = hashOps.get(skuId).toString();
            //序列化，更新数量
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);
        } else {
            //没有  新增购物车的记录、
            cart.setCheck(true);
            //查询sku的相关的信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {

                return;
            }

            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());

            //设置营销属性
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsFeign.querySkuSaleAttrValueBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrValueResp.getData();
            cart.setSkuAttrValue(skuSaleAttrValueEntities);
            //设置营销信息
            Resp<List<SaleVO>> saleVOResp = this.gmallSmsFeign.querySalesBySkuId(cart.getSkuId());
            List<SaleVO> saleVOS = saleVOResp.getData();
            cart.setSales(saleVOS);
            //设置库存的信息
            Resp<List<WareSkuEntity>> wareResp = this.gmallWmsFeign.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
                    return wareSkuEntity.getStock() > 0;
                }));
            }
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());

        }

        hashOps.put(skuId, JSON.toJSONString(cart));


    }

    public List<Cart> queryCarts() {
        //获取登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询未登录的购物车
        String unLoginKey = KEY_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        //获取购物车的集合，json格式的
        List<Object> cartJsonList = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            unLoginCarts = cartJsonList.stream().map(cartJson -> {
                //把json转化为具体的对象
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(priceString));
                return cart;
            }).collect(Collectors.toList());
        }

        //判断是否登录，如果未登录，直接返回
        if (userInfo.getId() == null) {
            return unLoginCarts;
        }

        //如果是登录状态，同步购物车
        String loginKey = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                Integer count = cart.getCount();
                //如果登录状态下的购物车有未登录的购物车的商品，更新次数
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }
                //登录的购物车与未登录的购物车不重叠时。直接把未登录的购物车设置给登录的购物车
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
            //更新完次数后，删除redis中的未登录的购物车
            this.redisTemplate.delete(unLoginKey);
        }

        //查询登录状态的购物车
        List<Object> loginCartJsonList = loginHashOps.values();
        List<Cart> carts = loginCartJsonList.stream().map(cartJson -> {
            Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
            //查询当前价格
            String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(priceString));
            return cart;
        }).collect(Collectors.toList());
        return carts;

    }

    public void updateCart(Cart cart) {
        String key = getLoginState();

        //获取购物车
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);

        Integer count = cart.getCount();
        //判断这条记录才购物车中有没有
        if (boundHashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = boundHashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            boundHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    public void deleteCart(Long skuId) {
        //获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获取redis中的key
        String key = getLoginState();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        hashOps.delete(skuId);

    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsonList  = hashOps.values();
        List<Cart> carts = cartJsonList.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).
                filter(Cart::getCheck).collect(Collectors.toList());
        return carts;

    }
}
