package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author lxf
 */
@Controller
public class ClientOrderController {
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private StoreService storeService;

    /**
     * @param login
     * @param page 页码
     * @param page_size 每页多少
     * @return 查询结果
     */
    @GetMapping("/orders")
    public @ResponseBody List<MainOrder> orderList(@AuthenticationPrincipal Login login, int page, int page_size){
        Store entity = storeService.findByLogin(login);
        if(entity == null){
            //TODO 还没写完
            //他是一个普通会员,查询自己的订单列表
        }
        return mainOrderService.search(entity,page,page_size);
    }

    @GetMapping("/orders/{orderId}")
    public @ResponseBody MainOrder mainOrderDetail(@PathVariable long orderId){
        MainOrder order = mainOrderService.findById(orderId);
        return order;
    }
}
