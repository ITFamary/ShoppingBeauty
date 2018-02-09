package com.ming.shopping.beauty.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.request.NewOrderBody;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.service.MainOrderService;
import me.jiangcai.crud.row.*;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;

/**
 * @author lxf
 */
@Controller
public class ClientMainOrderController {
    @Autowired
    private MainOrderService mainOrderService;

    /**
     * 门店代表下单
     *
     * @param login
     * @param postData
     */
    @PostMapping("/order")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('" + Login.ROLE_STORE_REPRESENT + "')")
    public void newOrder(@AuthenticationPrincipal Login login, @RequestBody NewOrderBody postData) {
        mainOrderService.supplementOrder(postData.getOrderId(), login.getRepresent(), postData.getItems());
    }

    /**
     * MainOrder列表
     * @param login
     * @return 查询结果
     */
    @GetMapping("/orders")
    @ResponseBody
    public void orderList(@AuthenticationPrincipal Login login
            , OrderSearcherBody postData, NativeWebRequest webRequest) throws IOException {
        if ("store".equalsIgnoreCase(postData.getOrderType())) {
            if (login.getRepresent() == null) {
                throw new ApiResultException(HttpStatusCustom.SC_FORBIDDEN);
            }
            postData.setStoreId(login.getRepresent().getStore().getId());
        } else {
            postData.setUserId(login.getId());
        }
        Page orderList = mainOrderService.findAll(postData);
        RowDramatizer dramatizer = new AntDesignPaginationDramatizer();
        dramatizer.writeResponse(orderList, mainOrderService.orderListField(), webRequest);
    }

    @GetMapping("/orders/{orderId}")
    @ResponseBody
    public void mainOrderDetail(@PathVariable long orderId, NativeWebRequest webRequest) throws IOException {
        OrderSearcherBody search = new OrderSearcherBody();
        search.setPageSize(1);
        search.setOrderId(orderId);
        Page orderList = mainOrderService.findAll(search);
        RowDramatizer dramatizer = new SingleRowDramatizer();
        dramatizer.writeResponse(orderList.getContent(),mainOrderService.orderListField(), webRequest);
    }
}
