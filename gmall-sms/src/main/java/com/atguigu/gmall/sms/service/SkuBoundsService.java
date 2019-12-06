package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.dto.SkuSaleDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品sku积分设置
 *
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 14:58:39
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSkuSaleInfo(SkuSaleDTO skuSaleDTO);
}

