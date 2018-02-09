package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.model.definition.ClientStoreItemModel;
import com.ming.shopping.beauty.service.service.StagingService;
import me.jiangcai.lib.resource.service.ResourceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static net.bytebuddy.implementation.FixedValue.value;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author CJ
 */
public class ClientItemControllerTest extends ClientConfigTest {

    @Autowired
    private static ResourceService resourceService;
    @Autowired
    private StagingService stagingService;
    /**
     * @return 校验器可以校验响应为 api 所规定的Items
     */
    public static ResultMatcher isItemsResponse() {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult result) throws Exception {
                jsonPath("$").value(matchModel(new ClientStoreItemModel(resourceService, false)));
            }
        };
    }


    @Test
    public void go() throws Exception {
        // TODO 添加测试数据
        
    }
}