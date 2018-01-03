package com.ming.shopping.beauty.service;

import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import me.jiangcai.lib.test.SpringWebTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author helloztt
 */
@ActiveProfiles({ServiceConfig.PROFILE_TEST, ServiceConfig.PROFILE_UNIT_TEST})
@ContextConfiguration(classes = CoreServiceTestConfig.class)
@WebAppConfiguration
public abstract class CoreServiceTest extends SpringWebTest {
    @Autowired
    protected MerchantRepository merchantRepository;
}
