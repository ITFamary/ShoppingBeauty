package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.JQueryDataTableDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * @author lxf
 */
@Controller
public class ClientMainOrderController {
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private StoreService storeService;
    /**
     * @param login
     * @return 查询结果
     */
    @GetMapping("/orders")
    @RowCustom(distinct = true ,dramatizer = JQueryDataTableDramatizer.class)
    public RowDefinition<MainOrder> orderList(@AuthenticationPrincipal Login login){
        Object entity = storeService.findByLogin(login);
        if(entity == null){
            entity = login.getUser();
        }
        return mainOrderService.search(entity);
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
                                .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderStatus))
                                .addFormat((data, type) -> ((OrderStatus) data).ordinal())
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "items")
                                .addSelect(root -> root.get(MainOrder_.orderItemList))
                                //TODO 2018/1/12 这里的返回的数据格式怎么改成前端需要的样子
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
