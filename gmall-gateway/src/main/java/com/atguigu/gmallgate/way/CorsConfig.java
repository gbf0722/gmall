package com.atguigu.gmallgate.way;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        //初始化cors对象
        CorsConfiguration configuration = new CorsConfiguration();
        //允许的域，不要写*，否则cookie 无法使用了
        configuration.addAllowedOrigin("http://localhost:1000");
        configuration.addAllowedOrigin("http://127.0.0.1:1000");
        //允许的头信息
        configuration.addAllowedHeader("*");
        //允许的请求方式
        configuration.addAllowedMethod("*");
        //允许的携带的cookie信息
        configuration.setAllowCredentials(true);

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);
        // cors过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}
