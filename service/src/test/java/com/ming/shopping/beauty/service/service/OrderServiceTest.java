package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.Order;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import me.jiangcai.jpa.entity.support.Address;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lxf
 */
public class OrderServiceTest extends CoreServiceTest {
    @Autowired
    private OrderService orderService;
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
        Item item = itemService.addItem(randomString(10), null, "测试添加项目", "测试", new BigDecimal(0.01),
                new BigDecimal(0.01), new BigDecimal(0.01), "测试添加一个项目", "这个项目用来测试", false);
        //生成订单项目
        OrderItem orderItem = orderItemService.newOrderItem(item, 1);
        assertThat(orderItem).isNotNull();
        //TODO 为啥不好使啊...
        //Order order = orderService.newOrder(null, null, null, orderItem);
    }
}
