package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.utils.Utils;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.JQueryDataTableDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import me.jiangcai.lib.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.criteria.JoinType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author lxf
 */
@Controller
public class ClientItemController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/items")
    @RowCustom(distinct = true, dramatizer = JQueryDataTableDramatizer.class)
    public void itemList(String item_type, int lat, int lon) {
        //TODO 带坐标的还不会写.
    }

    @GetMapping("/items/{itemId}")
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<Item> itemDetail(@PathVariable("itemId") long itemId) {
        return new RowDefinition<Item>() {
            @Override
            public Class<Item> entityClass() {
                return Item.class;
            }

            @Override
            public List<FieldDefinition<Item>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Item.class, "itemId")
                                .addSelect(root -> root.get(Item_.id))
                                .build()
                        , FieldBuilder.asName(Item.class, "thumbnail")
                                .addSelect(root -> root.get(Item_.mainImagePath))
                                .addFormat(Utils.formatResourcePathToURL(resourceService))
                                .build()
                        , FieldBuilder.asName(Item.class, "title")
                                .addSelect(root -> root.get(Item_.name))
                                .build()
                        , FieldBuilder.asName(Item.class, "address")
                                .addSelect(root -> root.join(Item_.merchant, JoinType.LEFT).get(Merchant_.address))
                                .addFormat((data, type) -> data.toString())
                                .build()
                        , FieldBuilder.asName(Item.class, "type")
                                .addSelect(root -> root.get(Item_.itemType))
                                .build()
                        , FieldBuilder.asName(Item.class, "distance")
                                //TODO 距离还不知道怎么写
                                .build()
                        , FieldBuilder.asName(Item.class, "vipPrice")
                                .addSelect(root -> root.get(Item_.salesPrice))
                                .build()
                        , FieldBuilder.asName(Item.class, "originalPrice")
                                .addSelect(root -> root.get(Item_.price))
                                .build()
                        , FieldBuilder.asName(Item.class, "details")
                                .addSelect(root -> root.get(Item_.richDescription))
                                .build()
                );
            }

            @Override
            public Specification<Item> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(Item_.id), itemId);
            }
        };
    }
}
