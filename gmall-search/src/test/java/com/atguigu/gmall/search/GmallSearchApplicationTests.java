package com.atguigu.gmall.search;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeign;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttr;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsFeign pmsApi;
    @Autowired
    private GmallWmsFeign wmsApi;

    @Autowired
    private GoodsRepository goodsRepository;


    @Test
    void contextLoads() {
        //创建索引和映射
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }

    @Test
        //导入数据的测试
    void importDate() {
        Long pageNum = 1L;
        Long pageSize = 100L;

        do {
            //1.分页查询spu
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = this.pmsApi.querySpusByPage(queryCondition);
            List<SpuInfoEntity> spus = listResp.getData();

            //2.遍历spu,查询sku;
            spus.forEach(spuInfoEntity -> {
                Resp<List<SkuInfoEntity>> skuResp = pmsApi.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (!CollectionUtils.isEmpty(skuInfoEntities)) {
                    //把sku转化为goods对象
                    List<Goods> goodlist = skuInfoEntities.stream().map(skuInfoEntity -> {
                        Goods goods = new Goods();
                        //查询搜索属性和值
                        Resp<List<ProductAttrValueEntity>> attrResp = this.pmsApi.querySearchAttrValue(spuInfoEntity.getId());
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
            });

            //导入索引库
            pageSize = (long) spus.size();
            pageNum++;


        } while (pageSize == 100);
    }

}
