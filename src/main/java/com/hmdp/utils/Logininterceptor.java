package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * @author Tonny
 * @date 2023/5/17 16:37
 * @version 2021.2
 */
public class Logininterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    public Logininterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }



    @Override
    //什么时候执行？
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //获取session
//        HttpSession session=request.getSession();
//        //获取session中的用户
//        Object user=session.getAttribute("user");
//        //判断用户是否存在,存在则保存，否则拦截
//        if(user==null){
//            response.setStatus(401);
//            return false;
//        }else{
//            UserHolder.saveUser((UserDTO) user);
//        }
//
//        //放行
//        return true;
        // 1.获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            return false;
        }
        String key  = LOGIN_USER_KEY + token;


        // 2.基于TOKEN获取redis中的用户
        Map<Object,Object> userMap= stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {

            response.setStatus(401);
            return false;

        }

        // 5.将查询到的hash数据转为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 6.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);

        // 7.刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.放行
        return true;

    }
}

