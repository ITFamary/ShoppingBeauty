package com.ming.shopping.beauty.controller;


import com.ming.shopping.beauty.client.config.ClientConfig;
import com.ming.shopping.beauty.manage.config.ManageConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * TODO 既然尚未写好 那就先抽象着
 */
@ContextConfiguration(classes = {ManageConfig.class,ClientConfig.class})
public abstract class TogetherTest extends CoreServiceTest {
}
