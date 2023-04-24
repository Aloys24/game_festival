package com.miniw.fescommon.interceptor;

import com.miniw.fescommon.utils.RedisUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 拦截器注册
 *
 * @author luoquan
 * @date 2021/08/03
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor(redisUtil))
                .addPathPatterns("/v1/festival/**")
                .excludePathPatterns("/static/**")
        ;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .allowedOriginPatterns("*");
    }
}
