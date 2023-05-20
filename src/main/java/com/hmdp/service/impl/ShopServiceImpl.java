package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        //防止缓存穿透
//        Shop shop=queryWithPassThrough(id);
        //互斥锁解决缓存击穿
        Shop shop=queryWithMutex(id);
        if(shop==null){
            return Result.fail("店铺不存在");
        }






        return Result.ok(shop);


    }
    //防止缓存穿透
    public Shop queryWithMutex(Long id){
        //从redis查询商铺缓存
        String key=CACHE_SHOP_KEY+id;
        String jsonshop= stringRedisTemplate.opsForValue().get(key);
        //存在则写回
        if(StrUtil.isNotBlank(jsonshop)){
            Shop shop= JSONUtil.toBean(jsonshop,Shop.class);
            return shop;

        }
        //判断是否为""
        if(jsonshop!=null){
            return null;
        }
        //实现缓存重建
        //获取互斥锁
        String lockkey="lock:shop"+id;
        Shop shop= null;
        try {
            boolean isLock=tryLock(lockkey);
            //判断是否获取成功
            if(!isLock){
                //失败，则休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id);

            }

            //成功，根据id查询数据库
            //模拟重建的延迟
            Thread.sleep(200);
            shop = getById(id);

            //返回


            //数据库查询失败
            if(shop==null){
                //将空值存入redis
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }

            //数据库中查询成功则写回redis
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //释放互斥锁
            unLock(lockkey);

        }






        return shop;


    }
    public Shop queryWithPassThrough(Long id){
        //从redis查询商铺缓存
        String key=CACHE_SHOP_KEY+id;
        String jsonshop= stringRedisTemplate.opsForValue().get(key);
        //存在则写回
        if(StrUtil.isNotBlank(jsonshop)){
            Shop shop= JSONUtil.toBean(jsonshop,Shop.class);
            return shop;

        }

        if(jsonshop!=null){
            return null;
        }

        //不存在则查询数据库
        Shop shop=getById(id);
        //数据库查询失败
        if(shop==null){
            //将空值存入redis
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }

        //数据库中查询成功则写回redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);





        return shop;


    }
    private boolean tryLock(String key){
        Boolean flag=stringRedisTemplate.opsForValue().setIfAbsent(key,"1",10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);

    }
    private void  unLock(String key){
        stringRedisTemplate.delete(key);
    }


    @Override
    public Result update(Shop shop) {
        Long id=shop.getId();
        if (id==null){
            return Result.fail("id不能为空");
        }
        //先改数据库
        updateById(shop);


        //再删缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY+id);
        return Result.ok();
    }
}
