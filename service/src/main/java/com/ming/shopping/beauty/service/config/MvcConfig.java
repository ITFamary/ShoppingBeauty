package com.ming.shopping.beauty.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.List;

/**
 * Created by helloztt on 2017/12/20.
 */
@Configuration
@EnableWebMvc
@Import({ServiceConfig.class})
@ComponentScan({"com.ming.shopping.beauty.service.controller","com.ming.shopping.beauty.service.converter"})
public class MvcConfig extends WebMvcConfigurerAdapter {

    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        converters.add(new AbstractHttpMessageConverter<AuditStatus>(){
            @Override
            protected boolean supports(Class<?> clazz) {
                return AuditStatus.class.equals(clazz);
            }

            @Override
            protected AuditStatus readInternal(Class<? extends AuditStatus> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                String inputString = mapper.readTree(inputMessage.getBody()).asText();
                logger.debug("greeting AuditStatus for "+inputString);
                return AuditStatus.valueOf(inputString);
            }

            @Override
            protected void writeInternal(AuditStatus status, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
throw new HttpMessageNotWritableException("我不干");
            }
        });
    }

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
