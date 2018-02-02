package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet_;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.settlement.SettlementSheetService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@RequestMapping("/settlementSheet")
@Controller
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@PreAuthorize("hasAnyRole('ROOT')")
public class ManageSettlementSheetController extends AbstractCrudController<SettlementSheet, Long> {

    @Autowired
    private SettlementSheetService settlementSheetService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 产生一个结算单
     *
     * @param merchantId
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/{merchantId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public ResponseEntity productSheet(@PathVariable("id") Long merchantId) throws URISyntaxException {
        Merchant merchant = merchantService.findMerchant(merchantId);
        SettlementSheet settlementSheet = settlementSheetService.productSheet(merchant);
        return ResponseEntity
                .created(new URI("/settlementSheet/" + settlementSheet.getId()))
                .build();
    }


    @GetMapping("/{id}/store")
    public RowDefinition<MainOrder> getDetailForStore(Long id) {
        return new RowDefinition<MainOrder>() {
            @Override
            public Class<MainOrder> entityClass() {
                return MainOrder.class;
            }

            @Override
            public CriteriaQuery<MainOrder> dataGroup(CriteriaBuilder cb, CriteriaQuery<MainOrder> query, Root<MainOrder> root) {
                return query.groupBy(
                        root.get(MainOrder_.store)
                );
            }

            @Override
            public List<FieldDefinition<MainOrder>> fields() {
                return Arrays.asList(
                        //结算金额
                        FieldBuilder.asName(MainOrder.class, "settlementAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.settlementAmount)))
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "actualAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.finalAmount)))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "count")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.count(mainOrderRoot))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "storeName")
                                .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.store))
                                .build()
                );
            }

            @Override
            public Specification<MainOrder> specification() {
                return null;
            }
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<SettlementSheet> root) {
        return super.listOrder(criteriaBuilder, root);
    }

    @Override
    protected List<FieldDefinition<SettlementSheet>> listFields() {
        return null;
    }

    @Override
    protected Specification<SettlementSheet> listSpecification(Map<String, Object> queryData) {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

}
