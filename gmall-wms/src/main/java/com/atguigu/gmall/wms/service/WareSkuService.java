package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 15:00:47
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    String checkAndLockStock(List<SkuLockVO> skuLockVOS);
}

