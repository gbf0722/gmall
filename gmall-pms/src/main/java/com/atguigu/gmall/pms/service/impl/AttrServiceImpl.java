package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidTypePage(QueryCondition condition, long cid, int type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        if (cid != 0) {
            wrapper.eq("catelog_id", cid);
        }
        wrapper.eq("attr_type", type);

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);

    }


    @Autowired

    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Override
    public void saveAttr(AttrVO attrVO) {
        //1.保存attr表
        this.save(attrVO);




        //2.保存attr_attrgroup_relation 中间表
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attrVO.getAttrId());
        this.attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);

    }

}