package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.dto.SkuSaleDTO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SkuSaleFeign;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.BaseAttrVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfo(QueryCondition condition, long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catalog_id", catId);

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SkuSaleFeign skuSaleFeign;

    @Override
    public void saveSpuInfoVO(SpuInfoVO spuInfoVO) {
        //1.保存spu 的相关信息
        // 1.1. 保存spu基本信息 spu_info表
        spuInfoVO.setPublishStatus(1);
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);  //保存到数据库中
        Long spuId = spuInfoVO.getId();  //获得新增后的spuid，操作下面的表，要用到spuid

        // 1.2. 保存spu的描述信息 spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        spuInfoDescEntity.setDecript(StringUtils.join(spuInfoVO.getSpuImages(), ","));
        spuInfoDescDao.insert(spuInfoDescEntity);

        // 1.3. 保存spu的规格参数信息
        List<BaseAttrVO> baseAttrs = spuInfoVO.getBaseAttrs();
        //先保存SpuInfoEntity的信息到数据库
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntits = baseAttrs.stream().map(productAttrValueEntity -> {
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrSort(0);
                productAttrValueEntity.setQuickShow(0);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(productAttrValueEntits);
        }


        //2.保存sku相关信息
        List<SkuInfoVO> skuInfoVOS = spuInfoVO.getSkus();


        if (CollectionUtils.isEmpty(skuInfoVOS)) {
            return;
        }
        skuInfoVOS.forEach(skuInfoVO -> {
            // 2.1. 保存sku基本信息
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
            //品牌和分类的id需要从spuInfo中获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            //随机获取UUID作为sku的编码
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            //获取图片列表
            List<String> images = spuInfoVO.getSpuImages();

            //如果图片列表不为null，则设置为默认图片
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg() == null ? images.get(0) : skuInfoEntity.getSkuDefaultImg());

            }
            skuInfoEntity.setSpuId(spuId);
            this.skuInfoDao.insert(skuInfoEntity);
            //获取skuid,下面几张表要用
            Long skuId = skuInfoEntity.getSkuId();

            //2.2保存sku 的图片信息
            if (!CollectionUtils.isEmpty(images)) {
                //获取默认的图片
                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    return skuImagesEntity;

                }).collect(Collectors.toList());
                //将图片的信息保存到数据库中
                this.skuImagesService.saveBatch(skuImagesEntities);
            }

            //2.3保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                //设置属性名，根据id查询AttrEntithy
                saleAttr.setSkuId(skuId);
                saleAttr.setAttrName(this.attrDao.selectById(saleAttr.getAttrId()).getAttrName());
                saleAttr.setAttrSort(0);

            });
            this.skuSaleAttrValueService.saveBatch(saleAttrs);
            // 3. 保存营销相关信息，需要远程调用gmall-sms
            // 3.1. 积分优惠

            // 3.2. 满减优惠

            // 3.3. 数量折扣
            SkuSaleDTO skuSaleDTO = new SkuSaleDTO();
            BeanUtils.copyProperties(skuInfoVO, skuSaleDTO);
            skuSaleDTO.setSkuId(skuId);
            //System.out.println(skuSaleDTO.toString());
            this.skuSaleFeign.saveSkuSaleInfo(skuSaleDTO);
        });


    }

}