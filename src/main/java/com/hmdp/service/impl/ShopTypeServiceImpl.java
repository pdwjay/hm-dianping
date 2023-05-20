package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;


//    @Override
//    public Result queryList() {
//        String key=CACHE_LIST_KEY;
//        //查询redis
//        String jsonList=stringRedisTemplate.opsForValue().get(key);
//
//        //命中则返回
//        if(StrUtil.isNotBlank(jsonList)){
////            JSONArray objects =JSONUtil.parseArray(json);
////            List<Map> maps1 = JSONUtil.toList(objects, Map.class);
////            JSONArray objects= JSONUtil.parseArray(jsonList);
//            List<ShopType> shoplist=JSONUtil.toList(jsonList,ShopType.class);
////            long total=shoplist.size();
//            return  Result.ok(shoplist);
//
//        }
//
//        //查询数据库
//        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
//
//        //不存在则返回失败
//        if(shopTypeList==null){
//            return Result.fail("查询失败");
//        }
//
//        //存入redis
//        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shopTypeList));
//
//        //返回数据
//
//
//        return Result.ok(shopTypeList);
//    }
}
