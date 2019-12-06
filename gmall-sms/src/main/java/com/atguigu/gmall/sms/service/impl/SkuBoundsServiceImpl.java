package com.atguigu.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dto.SkuSaleDTO;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private SkuFullReductionDao reductionDao;
    @Override
    public void saveSkuSaleInfo(SkuSaleDTO skuSaleDTO) {
        // 3.1. 保存sms_sku_bounds表
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleDTO.getSkuId());
        skuBoundsEntity.setBuyBounds(skuSaleDTO.getBuyBounds());
        skuBoundsEntity.setGrowBounds(skuSaleDTO.getGrowBounds());
        List<Integer> work = skuSaleDTO.getWork();
        skuBoundsEntity.setWork(work.get(0)*8+work.get(1)*4+work.get(2)*2+work.get(3)*1);
        this.save(skuBoundsEntity);


        // 3.2. 保存sms_sku_ladder表

        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuSaleDTO.getSkuId());
        skuLadderEntity.setFullCount(skuSaleDTO.getFullCount());
        skuLadderEntity.setDiscount(skuSaleDTO.getDiscount());
        skuLadderEntity.setAddOther(skuSaleDTO.getLadderAddOther());
        this.skuLadderService.save(skuLadderEntity);



        // 3.3. 保存sms_sku_full_reduction表
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setSkuId(skuSaleDTO.getSkuId());
        reductionEntity.setFullPrice(skuSaleDTO.getFullPrice());
        reductionEntity.setReducePrice(skuSaleDTO.getReducePrice());
        reductionEntity.setAddOther(skuSaleDTO.getFullAddOther());
        this.reductionDao.insert(reductionEntity);
    }

}