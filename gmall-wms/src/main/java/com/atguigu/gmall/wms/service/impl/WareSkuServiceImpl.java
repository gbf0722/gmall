package com.atguigu.gmall.wms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String checkAndLockStock(List<SkuLockVO> skuLockVOS) {
        if (CollectionUtils.isEmpty(skuLockVOS)) {
            return "没有选中的商品";
        }
        //检验并锁定库存
        skuLockVOS.forEach(skuLockVO -> {
            lockStroe(skuLockVO);
        });

        List<SkuLockVO> unLockSku = skuLockVOS.stream().filter(skuLockVO -> skuLockVO.getLock() == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unLockSku)) {
            //解锁已锁定的商品
            List<SkuLockVO> lockSku = skuLockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
            lockSku.forEach(skuLockVO -> {
                this.wareSkuDao.unLockStore(skuLockVO.getWareSkuId(), skuLockVO.getCount());
            });
            //提示锁定失败的商品
            List<Long> skuids = unLockSku.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "下单失败，商品库存不足" + skuids.toString();
        }

        return null;
    }

    //锁库存的方法
    private void lockStroe(SkuLockVO skuLockVO) {
        RLock lock = this.redissonClient.getLock("stock" + skuLockVO.getSkuId());
        lock.lock();
        //查询剩余库存够不够
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStore(skuLockVO.getSkuId(), skuLockVO.getCount());
        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            //锁定库存  锁第一个仓库的库存
            Long id = wareSkuEntities.get(0).getId();
            this.wareSkuDao.lockStore(id, skuLockVO.getCount());
            skuLockVO.setWareSkuId(id);
            skuLockVO.setLock(true);

        } else {
            skuLockVO.setLock(false);

        }

        lock.unlock();
    }

}