package com.ming.shopping.beauty.service.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author helloztt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemNum {
    private long storeItemId;
    private int num;
}
