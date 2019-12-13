package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * spu属性值
 *
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 04:03:53
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageVo queryPage(QueryCondition params);


    List<ProductAttrValueEntity> querySearchAttrValue(Long spuId);
}

