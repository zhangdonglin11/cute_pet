package com.example.cute_pet.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;


public class MyInterceptor implements HandlerInterceptor {

    public UserService userService;
    public MyInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("进入拦截器了");
        //中间写逻辑代码，比如判断是否登录成功，失败则返回false
        String token = request.getHeader("token");
        if (token!=null && TokenUtils.verify(token)){
            DecodedJWT decodedJWT = TokenUtils.getDecodedJWT(token);
            // 当前token的账号、密码是否和数据库表一样
            int id = decodedJWT.getClaim("id").asInt();
            String password = decodedJWT.getClaim("password").asString();
            User user = userService.getById(id);
            if (Objects.equals(password,user.getPassword())){
                return true;
            }else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //
            }

        }else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //
        }
        // System.out.println(authorization);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //
        // System.out.println("controller 执行完了");
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // System.out.println("我获取到了一个返回的结果："+response);
        // System.out.println("请求结束了");
    }
}