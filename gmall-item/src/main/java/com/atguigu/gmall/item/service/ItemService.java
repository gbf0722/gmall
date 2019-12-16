package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsFeign;
import com.atguigu.gmall.item.feign.GmallSmsFeign;
import com.atguigu.gmall.item.feign.GmallWmsFeign;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vo.SaleVO;

import java.util.Arrays;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Autowired
    private GmallSmsFeign gmallSmsFeign;

    @Autowired
    private GmallWmsFeign gmallWmsFeign;



    public ItemVO queryItemVO(Long skuId) {
        ItemVO itemVO = new ItemVO();
        itemVO.setSkuId(skuId);

        Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        if (skuInfoEntity == null) {
            return itemVO;
        }
        //根据Id查询sku
        itemVO.setSkuTitle(skuInfoEntity.getSkuTitle());
        itemVO.setSubTitle(skuInfoEntity.getSkuSubtitle());
        itemVO.setPrice(skuInfoEntity.getPrice());
        itemVO.setWeight(skuInfoEntity.getWeight());
        //获取spu的id；
        Long spuId = skuInfoEntity.getSpuId();

        //根据sku中的spuId查询spu
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsFeign.querySpuById(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        itemVO.setSpuId(spuId);
        if (spuInfoEntity != null) {
            itemVO.setSpuName(spuInfoEntity.getSpuName());
        }
        //根据sku查询图片列表
        Resp<List<SkuImagesEntity>> skuImagesResp = this.gmallPmsFeign.queryImagesBySkuId(skuId);
        List<SkuImagesEntity> skuImagesEntities = skuImagesResp.getData();
        itemVO.setPics(skuImagesEntities);

        //根据sku中的barndId 和categoryId 查询品牌和分类
        Resp<BrandEntity> brandEntityResp = this.gmallPmsFeign.brandInfo(skuInfoEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResp.getData();
        itemVO.setBrandEntity(brandEntity);
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsFeign.catInfo(skuInfoEntity.getCatalogId());
        CategoryEntity categoryEntity = categoryEntityResp.getData();
        itemVO.setCategoryEntity(categoryEntity);

        //根据skuid 查询营销信息
        Resp<List<SaleVO>> saleVOResp = this.gmallSmsFeign.querySalesBySkuId(skuId);
        List<SaleVO> saleVOS = saleVOResp.getData();
        itemVO.setSales(saleVOS);


        // 根据skuId查询库存信息
        Resp<List<WareSkuEntity>> wareResp = this.gmallWmsFeign.queryWareSkuBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareResp.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
            return wareSkuEntity.getStock()>0;
        }));


        //根据spuId查询所有的skuIds 再去查询所有的销售属性
        Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsFeign.querySkuSaleAttrValuesBySpuId(spuId);
        List<SkuSaleAttrValueEntity> SkuSaleAttrValueEntities = saleAttrValueResp.getData();
        itemVO.setSaleAttrs(SkuSaleAttrValueEntities);

        //根据spuid查询商品的描述
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsFeign.querySpuDescBySpuId(spuId);
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
        if (spuInfoDescEntity != null) {
            String decript = spuInfoDescEntity.getDecript();
            String[] split = StringUtils.split(decript, ",");
            itemVO.setImages(Arrays.asList(split));
        }


        //根据spuid和cateid 查询组和组下的规格参数（带值
        Resp<List<ItemGroupVO>> itemGroupResp = this.gmallPmsFeign.queryItemGroupVOByCidAndSpuId(skuInfoEntity.getCatalogId(), spuId);
        List<ItemGroupVO> itemGroupVOS = itemGroupResp.getData();


        itemVO.setGroups(itemGroupVOS);

        return itemVO;

    }
}
