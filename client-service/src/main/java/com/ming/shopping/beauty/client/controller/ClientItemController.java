package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.service.ItemService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.List;

/**
 * @author lxf
 */
@Controller
public class ClientItemController {

    @GetMapping("/items")
    public void itemList(String item_type,int lat,int lon,int page,int page_size){
        //TODO 带坐标的还不会写.
    }
    @GetMapping("/items/{itemId}")
    @RowCustom(distinct = true)
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
                                .addSelect(root -> root.get(Item_.thumbnailUrl))
                                .build()
                        , FieldBuilder.asName(Item.class, "title")
                                .addSelect(root -> root.get(Item_.name))
                                .build()
                        , FieldBuilder.asName(Item.class, "address")
                                .addSelect(root -> root.join(Item_.merchant, JoinType.LEFT).get(Merchant_.address))
                                .build()
                        , FieldBuilder.asName(Item.class, "type")
                                .addSelect(root -> root.get(Item_.itemType))
                                .build()
                        , FieldBuilder.asName(Item.class, "distance")
                                //TODO 距离还不知道怎么写
                                .build()
                        , FieldBuilder.asName(Item.class, "vipPrice")
                                //TODO 会员价是这个么
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
