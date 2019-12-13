package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 属性&属性分组关联
 *
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 04:03:53
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageVo queryPage(QueryCondition params);

    void delteAttr(List<AttrAttrgroupRelationEntity> attrAttrGroupEntityList);
}

