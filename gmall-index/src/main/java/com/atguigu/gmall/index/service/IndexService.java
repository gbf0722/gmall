package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cates";


    public List<CategoryEntity> queryLvl1Categories() {

        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByPidOrLevel(1, 0L);
        List<CategoryEntity> categoryEntities = listResp.getData();

        return categoryEntities;
    }

    public List<CategoryVO> querySubCategories(int pid) {
        //从缓存中获取
        String cateJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (!StringUtils.isEmpty(cateJson)) {
            return JSON.parseArray(cateJson, CategoryVO.class);

        }
        //如果缓存中没有则调用远程接口获取
        List<CategoryVO> categoryVOS = this.gmallPmsClient.querySubCategories(pid);
        //把查询结果放入缓存中
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS));

        return categoryVOS;
    }


    public void testLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,2, TimeUnit.SECONDS);
        if (lock) {

            //查看redis中的Num的值
            String value = this.redisTemplate.opsForValue().get("num");
            //如果没有该值
            if (StringUtils.isBlank(value)) {
                return;
            }
            //有值就转换成int
            int num = Integer.parseInt(value);
            //把redis中的值加1
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //释放锁
            //this.redisTemplate.delete("lock");
           if (StringUtils.equals(redisTemplate.opsForValue().get("lock"), uuid)) {
                this.redisTemplate.delete("lock");
            }
          /* String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return " +
                    "redis.call('del', KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script),
                    Arrays.asList("lock"), Arrays.asList(uuid));*/
        } else {


                testLock();


        }
    }

    public void testLock01(){

        //获取锁
        RLock lock = this.redissonClient.getLock("lock");
        //枷锁
        lock.lock(10, TimeUnit.SECONDS);
        //查询redis中的Num的值
        String value = this.redisTemplate.opsForValue().get("num");
        //如果没有该值   直接返回
        if (StringUtils.isBlank(value)) {
            return ;
        }
        //有值就转化为Int
        int num = Integer.parseInt(value);
        // 把redis中的num 加1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
        //接触锁
        //lock.unlock();

    }
}
