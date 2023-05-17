package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Tonny
 * @date 2023/5/17 16:37
 * @version 2021.2
 */
public class Logininterceptor implements HandlerInterceptor {


    @Override
    //什么时候执行？
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session
        HttpSession session=request.getSession();
        //获取session中的用户
        Object user=session.getAttribute("user");
        //判断用户是否存在,存在则保存，否则拦截
        if(user==null){
            response.setStatus(401);
            return false;
        }else{
            UserHolder.saveUser((UserDTO) user);
        }

        //放行
        return true;

    }
}

