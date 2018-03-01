package com.ming.shopping.beauty.service.model.request;

import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Data;

/**
 * {@link /orders} 查询订单请求体
 *
 * @author helloztt
 */
@Data
public class OrderSearcherBody {
    private String orderType = "member";
    private int current = 1;
    private int pageSize = Constant.CLIENT_PAGE_SIZE;

    private Long orderId;
    /**
     * 查找这之后的订单数据（用于下拉刷新）
     */
    private Long lastOrderId;
    private Long userId;
    private Long representId;
    private Long storeId;

    private OrderStatus orderStatus;

    // TODO: 2018/1/13 管理后台的查询也可用此Body
}
