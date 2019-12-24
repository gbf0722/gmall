package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeign;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttr;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {


    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsFeign pmsApi;
    @Autowired
    private GmallWmsFeign wmsApi;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gmall-search-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key ={"item.insert","item.update"}
    ))
    public void listener(Long spuId) {
        Resp<List<SkuInfoEntity>> skuResp = pmsApi.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            //把sku转化为goods对象
            List<Goods> goodlist = skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                //查询搜索属性和值
                Resp<List<ProductAttrValueEntity>> attrResp = this.pmsApi.querySearchAttrValue(spuId);
                List<ProductAttrValueEntity> attrValueEntities = attrResp.getData();
                if (!CollectionUtils.isEmpty(attrValueEntities)) {
                    List<SearchAttr> searchAttrs = attrValueEntities.stream().map(productAttrValueEntity -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrs);
                }

                //  查询品牌
                Resp<BrandEntity> brandEntityResp = this.pmsApi.brandInfo(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(skuInfoEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }

                //查询分类
                Resp<CategoryEntity> categoryEntityResp = this.pmsApi.catInfo(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                    goods.setCategoryName(categoryEntity.getName());

                }

                Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsApi.querySpuById(spuId);
                SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                goods.setCreateTime(spuInfoEntity.getCreateTime());
                goods.setPic(skuInfoEntity.getSkuDefaultImg());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                goods.setSale(0L);
                goods.setSkuId(skuInfoEntity.getSkuId());

                //查询库存信息
                Resp<List<WareSkuEntity>> wareSkuResp = this.wmsApi.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();

                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    Boolean flag=false;
                    flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> {

                        return wareSkuEntity.getStock()>0;
                    });
                    goods.setStore(flag);
                    System.out.println(goods.getStore());
                }
                goods.setTitle(skuInfoEntity.getSkuTitle());

                return goods;


            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodlist);
        }
    }
}
