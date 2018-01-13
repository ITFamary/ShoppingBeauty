package com.ming.shopping.beauty.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.row.*;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author lxf
 */
@Controller
public class ClientMainOrderController {
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private StoreService storeService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param login
     * @return 查询结果
     */
    @GetMapping("/orders")
    @ResponseBody
    public void orderList(@AuthenticationPrincipal Login login
            , @RequestBody OrderSearcherBody postData, NativeWebRequest webRequest) throws IOException {
        if ("store".equalsIgnoreCase(postData.getOrderType())) {
            if (login.getRepresent() == null) {
                throw new ApiResultException(HttpStatusCustom.SC_FORBIDDEN);
            }
            postData.setStoreId(login.getRepresent().getStore().getId());
        } else {
            postData.setUserId(login.getId());
        }
        List orderList = mainOrderService.findAll(postData);
        RowDramatizer dramatizer = new DefaultRowDramatizer();
        dramatizer.writeResponse(orderList, mainOrderService.orderListField(), webRequest);
    }

    @GetMapping("/orders/{orderId}")
    public RowDefinition<MainOrder> mainOrderDetail(@PathVariable long orderId) {
        return new RowDefinition<MainOrder>() {
            @Override
            public Class<MainOrder> entityClass() {
                return MainOrder.class;
            }

            @Override
            public List<FieldDefinition<MainOrder>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(MainOrder.class, "orderId")
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "orderStatus")
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "orderStatusCode")
                                .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderStatus))
                                .addFormat((data, type) -> ((OrderStatus) data).ordinal())
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "items")
                                .addSelect(root -> root.get(MainOrder_.orderItemList))
                                .addFormat((data, type) -> getOrderItemResult(data))
                                .build()
                );
            }

            @Override
            public Specification<MainOrder> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(MainOrder_.orderId), orderId);
            }
        };
    }

    private List<FieldDefinition<MainOrder>> listFieldForUser() {
        return Arrays.asList(
                FieldBuilder.asName(MainOrder.class, "orderId")
                        .build()
                , FieldBuilder.asName(MainOrder.class, "completeTime")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.payTime))
                        .addFormat((data, type) -> ((LocalDateTime) data).toString())
                        .build()
                , FieldBuilder.asName(MainOrder.class, "orderStatus")
                        .build()
                , FieldBuilder.asName(MainOrder.class, "store")
                        .build()
                , FieldBuilder.asName(MainOrder.class, "payer")
                        .build()
                , FieldBuilder.asName(MainOrder.class, "payerMobile")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.payer).get(User_.login).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "items")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderItemList))
                        .addFormat((data, type) -> getOrderItemResult(data))
                        .build()
        );
    }

    @SuppressWarnings("unchecked")
    private String getOrderItemResult(Object data) {
        Map<String, Object> result = new HashMap<>();
        ((List<OrderItem>) data).forEach((item) -> {
            result.put("itemId", item.getItemId());
            result.put("thumbnail", item.getItem().getThumbnailUrl());
            result.put("title", item.getName());
            result.put("quantity", item.getNum());
            result.put("amount", item.getCostPrice());
        });
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
