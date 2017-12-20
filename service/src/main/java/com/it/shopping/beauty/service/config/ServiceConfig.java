package com.it.shopping.beauty.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by helloztt on 2017/12/21.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan({"com.it.shopping.beauty.service.service"})
@EnableJpaRepositories(basePackages = "com.it.shopping.beauty.service.repository")
@ImportResource({"classpath:service_config_prod.xml","classpath:service_config_test.xml"})
public class ServiceConfig {
}
