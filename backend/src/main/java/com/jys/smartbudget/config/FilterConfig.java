package com.jys.smartbudget.config;

import com.jys.smartbudget.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration      //Spring이 이 클래스를 설정(bean 설정 파일) 으로 인식
@RequiredArgsConstructor
public class FilterConfig {

    private final RedisTokenService redisTokenService;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {      //Spring Boot의 서블릿 필터 등록 방식, Controller보다 앞단에서 실행될 필터를 등록할 때 사용
        FilterRegistrationBean<JwtAuthFilter> filterBean = new FilterRegistrationBean<>();

        filterBean.setFilter(new JwtAuthFilter(redisTokenService));
        filterBean.addUrlPatterns("/budgets/*"); 
        filterBean.addUrlPatterns("/expenses/*");
        filterBean.setOrder(1);

        return filterBean;
    }
}
