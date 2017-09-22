package com.cmss.xgg.videodemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.Filter;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = false)
public class Application {

    /**
     * 处理编码-统一转换成UTF8
     *
     * @return
     */
    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
