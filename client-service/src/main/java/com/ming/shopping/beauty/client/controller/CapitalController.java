package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import com.ming.shopping.beauty.service.entity.log.CapitalFlow_;
import com.ming.shopping.beauty.service.entity.login.Login;
import me.jiangcai.crud.row.DefaultRowDramatizer;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/capital")
public class CapitalController {
    @Autowired
    private ConversionService conversionService;

    @GetMapping("/flow")
    @RowCustom(distinct = true, dramatizer = DefaultRowDramatizer.class)
    public RowDefinition<CapitalFlow> capitalFlow(@AuthenticationPrincipal Login login) {
        return new RowDefinition<CapitalFlow>() {
            @Override
            public Class<CapitalFlow> entityClass() {
                return CapitalFlow.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<CapitalFlow> root) {
                return Arrays.asList(
                        criteriaBuilder.desc(root.get(CapitalFlow_.id))
                );
            }

            @Override
            public List<FieldDefinition<CapitalFlow>> fields() {
                return listFields();
            }

            @Override
            public Specification<CapitalFlow> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(CapitalFlow_.userId), login.getId());
            }
        };
    }

    private List<FieldDefinition<CapitalFlow>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(CapitalFlow.class, "time")
                        .addSelect(root -> root.get(CapitalFlow_.happenTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "title")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "sum")
                        .addSelect(root -> root.get(CapitalFlow_.changed))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "type")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> ((Enum) data).ordinal())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "orderId")
                        .addSelect(root -> root.get(CapitalFlow_.id))
                        .build()
        );
    }
}
