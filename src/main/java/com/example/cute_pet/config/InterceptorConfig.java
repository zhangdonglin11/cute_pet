package com.example.cute_pet.config;

import com.example.cute_pet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private UserService userService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyInterceptor(userService))
                .addPathPatterns("/api/user/mine/pet")
                .addPathPatterns("/api/user/mine/like")
                .addPathPatterns("/api/user/pet") //保存宠物
                .addPathPatterns("/api/user/pet/draft") //宠物草稿
                .addPathPatterns("/api/user/pet/pic") //保存宠物图片
                .addPathPatterns("/api/user/pet/delete") //删除宠物
                .addPathPatterns("/api/user/pet/like") //点赞宠物
                .addPathPatterns("/api/user/pet/comment") //发表评论
                .addPathPatterns("/api/user/topic") // 单个话题
                .addPathPatterns("/api/user/topic/pic") //保存图片
                .addPathPatterns("/api/user/topic/draft") //初始化的话题编辑
                .addPathPatterns("/api/user/topic/delete") //删除话题
                .addPathPatterns("/api/user/topic/list") // 获取列表话题
                .addPathPatterns("/api/user/topic/comment") //发表评论
                .addPathPatterns("/api/user/topic/like") //

                // .addPathPatterns("/*")//拦截所有的路径
                .excludePathPatterns("/LoginController/login")
                .excludePathPatterns("/login");

        // 添加第二个拦截器并设置拦截条件 后台管理拦截器
        // registry.addInterceptor(new AdminInterceptor())
        //         .addPathPatterns("/api/admin/*")
        //         .excludePathPatterns("/api/admin/login");
    }
}