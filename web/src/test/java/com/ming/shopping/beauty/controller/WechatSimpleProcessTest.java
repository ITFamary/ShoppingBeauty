package com.ming.shopping.beauty.controller;

import com.ming.shopping.beauty.service.service.InitService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <ol>
 * <li>构建商户，门店，项目以及门店项目；还有门店代表 应该在main代码中，因为staging也需要自己完成这么一份数据</li>
 * <li>用户访问/items 可以获得门店项目；而不是项目，应该校验结果是否仅仅在项目中或者未被激活 还有格式检测</li>
 * <li>用户访问/users/vipCard 可以获得订单号；以及被扫码的地址</li>
 * <li>门店代表 可以根据自己的storeId 访问/items 获取他们自己的门店项目</li>
 * <li>门店代表 可以通过post /order 完成下单</li>
 * <li>用户 可以通过 PUT /capital/payment/{orderId} 完成支付</li>
 * <li>门店代表 可以通过 get /orders 获得该用户已支付的信息</li>
 * </ol>
 *
 * @author CJ
 */
public class WechatSimpleProcessTest extends TogetherTest {

    @Autowired
    private InitService initService;

    @Test
    public void flow() {
        // 1
//        Object[] genratingData = initService.generateStagingData();

    }

}
