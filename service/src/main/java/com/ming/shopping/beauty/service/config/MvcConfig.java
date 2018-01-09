package com.ming.shopping.beauty.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by helloztt on 2017/12/20.
 */
@Configuration
@EnableWebMvc
@Import({ServiceConfig.class})
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.extendMessageConverters(converters);
        // 必须确保 json MappingJackson2HttpMessageConverter 比 xml MappingJackson2XmlHttpMessageConverter 优先级高
        HttpMessageConverter xml = converters.stream().filter(httpMessageConverter
                -> httpMessageConverter instanceof MappingJackson2XmlHttpMessageConverter)
                .findAny().orElse(null);

        HttpMessageConverter json = converters.stream().filter(httpMessageConverter
                -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter)
                .findAny().orElse(null);

        if (xml != null && json != null) {
            int index = converters.indexOf(xml);
            converters.remove(json);
            converters.add(index, json);
        }
    }

    /**
     * 文件上传
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}
