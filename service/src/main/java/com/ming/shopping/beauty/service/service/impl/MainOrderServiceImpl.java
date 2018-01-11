package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.service.MainOrderService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class MainOrderServiceImpl implements MainOrderService {

    @Autowired
    private MainOrderRepository mainOrderRepository;


    @Override
    public MainOrder findById(long id) {
        MainOrder one = mainOrderRepository.findOne(id);
        if (one == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MAINORDER_NOT_EXIST));
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder newEmptyOrder(User user) {
        //先看看这个用户有没有空的订单
        MainOrder mainOrder = mainOrderRepository.findEmptyOrderByPayer(user.getId());
        if(mainOrder != null){
            return mainOrder;
        }
        mainOrder = new MainOrder();
        mainOrder.setPayer(user);
        //空的订单
        mainOrder.setOrderStatus(OrderStatus.EMPTY);
        return mainOrderRepository.save(mainOrder);
    }

    @Override
    @Transactional
    public MainOrder newOrder(Store store, User user, Represent represent, List<OrderItem> itemList) {
        // TODO: 2018/1/12 这里其实应该是补充订单而不是新增订单，在会员卡页面的二维码已经生成了一个空的订单
        //，门店代表扫码后，把List<OrderItem>塞到了这个订单里，并修改MainOrder
        MainOrder mainOrder = new MainOrder();
        //order.setCreateTime(LocalDateTime.now());
        mainOrder.setStore(store);
        mainOrder.setPayer(user);
        mainOrder.setRepresent(represent);
        mainOrder.setOrderItemList(itemList);
        //待付款
        mainOrder.setOrderStatus(OrderStatus.forPay);
        //未结算
        mainOrder.setSettled(false);
        return mainOrderRepository.save(mainOrder);
    }

    @Override
    @Transactional
    public MainOrder newOrder(Store store, User user, Represent represent, OrderItem orderItem) {
        List<OrderItem> list = new ArrayList<>();
        list.add(orderItem);
        return newOrder(store, user, represent, list);
    }

    @Override
    public boolean payOrder(long id) {
        //TODO 还不知道怎么写
        MainOrder mainOrder = findById(id);
        mainOrder.setPayTime(LocalDateTime.now());
        mainOrder.setOrderStatus(OrderStatus.success);
        return false;
    }

    @Override
    public List<MainOrder> search(Object entity, int page, int page_size) {
        Pageable pageable =
                new PageRequest(page - 1, page_size, new Sort(Sort.Direction.DESC, MainOrder_.createTime.getName()));
        final Page<MainOrder> result;
        if (entity instanceof Store) {
            result = mainOrderRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(MainOrder_.store), entity)), pageable);
            new RowDefinition<MainOrder>() {
                @Override
                public Class<MainOrder> entityClass() {
                    return MainOrder.class;
                }

                @Override
                public List<FieldDefinition<MainOrder>> fields() {
                    return Arrays.asList(
                            FieldBuilder.asName(MainOrder.class, "orderId")
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "completeTime")
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "orderStatus")
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "store")
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "payer")
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "payerMobile")
                                    // 比你想象的要简单
                                    .addSelect(mainOrderRoot -> {
                                        // 这里我将写成一步一步的方式
                                        // 首先获得支付者
                                        Join<?, User> userJoin = mainOrderRoot.join(MainOrder_.payer);// 当然也直接用get 使用join可以自定以←或者→链接
                                        // 获得其Login
                                        Join<?, Login> loginJoin = userJoin.join(User_.login);// 跟上列一样
                                        // 让后获得它的电话号码
                                        return loginJoin.get(Login_.loginName);
                                    })
                                    // 在确认内连接没有问题的情况下-> 内链接会产生其他约束 关联的所有数据绝不为空，否者会被本次查询所过滤
                                    // 就可以采用以下的简单写法
//                                    .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.payer).get(User_.login).get(Login_.loginName))
                                    .build()
                            , FieldBuilder.asName(MainOrder.class, "items")
                                    .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderItemList))
                                    .build()
                    );
                }

                @Override
                public Specification<MainOrder> specification() {
                    //TODO 如何分页? 此处命题错误，RowDefinition只定义数据规格和渲染方式，简单的说它跟分页无关，如果真的产生了关系也是渲染器干的
                    //TODO 如果要立刻获得其结果 应该采用 me.jiangcai.crud.row.RowService 的几个方法；具体可以打开这个接口 并且download source;可以在此直接 Autowired
                    //return (root, query, cb) -> cb.and(cb.equal(root.get(MainOrder_.store), entity),pageable);
                    return null;
                }
            };
        } else if (entity instanceof User) {
            result = mainOrderRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(MainOrder_.payer), entity)), pageable);
        } else {
            return null;
        }
        return result.getContent();
    }
}
