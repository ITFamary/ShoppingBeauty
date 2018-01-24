package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store_;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.criteria.Predicate;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@Controller
@RequestMapping("/storeItem")
@PreAuthorize("hassAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','"+Login.ROLE_STORE_ROOT+"')")
public class ManageStoreItemController extends AbstractCrudController<StoreItem, Long> {



    //添加/编辑门店项目
    @PostMapping
    @Override
    public ResponseEntity addOne(StoreItem postData, Map<String, Object> otherData) throws URISyntaxException {
        return super.addOne(postData, otherData);
    }

    @Override
    protected List<FieldDefinition<StoreItem>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(StoreItem.class, "id")
                        .build()
                , FieldBuilder.asName(StoreItem.class, "name")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class,"storeName")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.store).get(Store_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class,"price")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.price))
                        .build()
                , FieldBuilder.asName(StoreItem.class,"salesPrice")
                        .build()
                , FieldBuilder.asName(StoreItem.class,"enabled")
                        .addSelect(storeItemRoot -> storeItemRoot.get(StoreItem_.enable))
                        .build()
                , FieldBuilder.asName(StoreItem.class,"recommended")
                        .build()
        );
    }

    @Override
    protected Specification<StoreItem> listSpecification(Map<String, Object> queryData) {
        return ((root, query, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("itemName") != null)
                conditionList.add(cb.like(root.join(StoreItem_.item).get(Item_.name), "%" + queryData.get("itemName") + "%"));
            if (queryData.get("storeName") != null)
                conditionList.add(cb.like(root.join(StoreItem_.store).get(Store_.name), "%" + queryData.get("storeName") + "%"));
            if (queryData.get("enabled") != null) {
                if ((boolean) queryData.get("enabled"))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.enable)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.enable)));
            }
            if (queryData.get("recommended") != null) {
                if ((boolean) queryData.get("recommended"))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.recommended)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.recommended)));
            }
            conditionList.add(cb.isFalse(root.get(StoreItem_.deleted)));
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        });
    }
}
