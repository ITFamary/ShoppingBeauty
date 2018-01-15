package com.ming.shopping.beauty.service.model.request;

import com.ming.shopping.beauty.service.entity.item.StoreItem;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author helloztt
 */
@Data
public class NewOrderBody {
    private long orderId;
    private StoreItemNum[] items;
}
