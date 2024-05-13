package com.example.cute_pet.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;


public class AdminInterceptor implements HandlerInterceptor {

    public UserService userService;
    public AdminInterceptor() {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("进入拦截器了");
        //中间写逻辑代码，比如判断是否登录成功，失败则返回false
        String token = request.getHeader("token");
        if (token!=null && TokenUtils.verify(token)){
            if (TokenUtils.verify(token)){
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
        System.out.println("controller 执行完了");
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        System.out.println("我获取到了一个返回的结果："+response);
        System.out.println("请求结束了");
    }
}