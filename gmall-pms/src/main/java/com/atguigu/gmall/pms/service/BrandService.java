package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 品牌
 *
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 04:03:53
 */
public interface BrandService extends IService<BrandEntity> {

    PageVo queryPage(QueryCondition params);
}

