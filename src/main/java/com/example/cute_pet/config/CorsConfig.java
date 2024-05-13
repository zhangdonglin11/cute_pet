package com.example.cute_pet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 允许跨域的路径
                        .allowedOrigins("*") // 允许跨域的源
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方法
                        .maxAge(3600); // 预检请求的有效期，单位为秒。设置为 3600 秒，即一小时内不需要再发送预检请求
            }
        };
    }
}
