package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 14:58:40
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
