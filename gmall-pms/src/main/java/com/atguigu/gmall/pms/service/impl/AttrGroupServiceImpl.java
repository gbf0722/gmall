package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidPage(QueryCondition condition, long catId) {
        QueryWrapper<AttrGroupEntity> wrpper = new QueryWrapper<>();
        if (catId != 0) {
            wrpper.eq("catelog_id", catId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                wrpper
        );

        return new PageVo(page);
    }

    @Autowired
    private ProductAttrValueDao attrValueDao;

    @Override
    public GroupVO queryById(long gid) {
        GroupVO groupVO = new GroupVO();
        //1.先查询attr_group 表

        AttrGroupEntity attrGroupEntity = this.getById(gid);
        BeanUtils.copyProperties(attrGroupEntity, groupVO);
        //2.查询attr_attrgroup_relation表
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_group_id", gid);
        List<AttrAttrgroupRelationEntity> relations = this.attrAttrgroupRelationDao.selectList(wrapper);
        if (CollectionUtils.isEmpty(relations)) {
            return groupVO;
        }
        System.out.println(relations.toString());
        groupVO.setRelations(relations);
        //得到attr_id的集合，用来查询attr表
        List<Long> attrIds = relations.stream().map(relationEntity -> relationEntity.getAttrId()).collect(Collectors.toList());
        //3.查询attr表
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrEntities);

        return groupVO;
    }


    @Autowired
    private AttrGroupDao attrGroupDao;

    @Override
    public List<GroupVO> queryByCid(Long cid) {

        //先查询attr_group这张表的信息
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", cid);
        //List<AttrGroupEntity> attrGroupEntities = this.attrGroupDao.selectList(wrapper);
        List<AttrGroupEntity> attrGroupEntities = this.list(wrapper);

        //查询每组下的规格参数

        // 1.根据分组中id查询中间表
        // 2.根据中间表中attrIds查询参数
        // 3.数据类型的转化：attrGroupEntity-->groupVO
        List<GroupVO> groupVOS = attrGroupEntities.stream().map(attrGroupEntity -> {
            return this.queryById(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());


        return groupVOS;
    }

    @Override
    public List<ItemGroupVO> queryItemGroupVOByCidAndSpuId(Long cid, Long spuId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", cid);
        List<AttrGroupEntity> attrGroupEntities = this.list(wrapper);

        List<ItemGroupVO> itemGroupVOs = attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVO itemGroupVO = new ItemGroupVO();
            itemGroupVO.setName(attrGroupEntity.getAttrGroupName());

            //查询规格参数和值
            QueryWrapper<AttrAttrgroupRelationEntity> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("attr_group_id", attrGroupEntity.getAttrGroupId());
            List<AttrAttrgroupRelationEntity> relationEntities = this.attrAttrgroupRelationDao.selectList(wrapper1);
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

            QueryWrapper<ProductAttrValueEntity> wrapper2 = new QueryWrapper<>();
            wrapper2.in("attr_id", attrIds).eq("spu_id", spuId);
            List<ProductAttrValueEntity> productAttrValueEntities = this.attrValueDao.selectList(wrapper2);
            itemGroupVO.setBaseAttr(productAttrValueEntities);
            return itemGroupVO;
        }).collect(Collectors.toList());


        return itemGroupVOs;
    }


}