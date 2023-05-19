package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexPatterns;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //test

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis,且设置key的有效期
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL,TimeUnit.MINUTES);


        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回ok
        return Result.ok();




    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)){
            //3.不一致，报错
            return Result.fail("验证码错误");
        }
//        QueryWrapper queryWrapper=new QueryWrapper();
//        queryWrapper.eq("phone",phone);
//        User user=userMapper.selectOne(queryWrapper);
        User user = query().eq("phone", phone).one();
        if(user==null){
            //创建用户
            user =  createUserWithPhone(phone);


        }
        //保存用户到redis中
        //随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //将user对象转为hash 存储
        UserDTO userDTO=BeanUtil.copyProperties(user,UserDTO.class);
        Map<String,String> userMap=new HashMap<String,String>();
//        Map<String,Object> userMap=BeanUtil.beanToMap(userDTO,new HashMap<>(),
//                CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        userMap.put("id",userDTO.getId().toString());
        userMap.put("nickName",userDTO.getNickName());
        userMap.put("icon",userDTO.getIcon());

//        Map<String, String> myMap = new HashMap<String, String>();
//        Map<String,String> userMap=new HashMap<String,String>();
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        //返回token给客户端
        //存的是 UserDto对象不是user对象

        return Result.ok(token);




    }

    private User createUserWithPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return  user;

    }
}
