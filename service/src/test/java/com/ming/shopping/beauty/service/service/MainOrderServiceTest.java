package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lxf
 */
public class MainOrderServiceTest extends CoreServiceTest {
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private StoreService storeService;
    @Test
    public void go(){
        //生成一个订单.
        //首先有个项目
        Item item = itemService.addItem(null, "测试添加项目", "测试", new BigDecimal(0.01),
                new BigDecimal(0.01), new BigDecimal(0.01), "测试添加一个项目", "这个项目用来测试", false);
        //生成订单项目
        OrderItem orderItem = orderItemService.newOrderItem(item, 1);
        assertThat(orderItem).isNotNull();
        //TODO 为啥不好使啊...
        MainOrder order = mainOrderService.newOrder(null, null, null, orderItem);
    }
}
