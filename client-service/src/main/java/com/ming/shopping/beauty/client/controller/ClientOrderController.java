package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.JoinType;
import java.util.Arrays;
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
    @RowCustom(distinct = true)
    public RowDefinition<MainOrder> mainOrderDetail(@PathVariable long orderId){
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
                                //TODO 
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "items")
                                .addSelect(root -> root.get(MainOrder_.orderItemList))
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
}
