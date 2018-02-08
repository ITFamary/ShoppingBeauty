package com.ming.shopping.beauty.controller;


import com.ming.shopping.beauty.client.config.ClientConfig;
import com.ming.shopping.beauty.manage.config.ManageConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {ManageConfig.class, ClientConfig.class})
public abstract class TogetherTest extends CoreServiceTest {
}
