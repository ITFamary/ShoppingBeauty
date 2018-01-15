package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 门店项目
 *
 * @author helloztt
 */
@Entity
@Getter
@Setter
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    @ManyToOne
    private Item item;
    /**
     * todo 这里应该给不同等级的用户设置不同的销售价
     * 销售价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal salesPrice;
    /**
     * 是否推荐
     */
    private boolean recommended;
    /**
     * 是否上架
     */
    private boolean enable = true;
    /**
     * 含义上跟enable完全不同；该值为true 则该货品不会在系统中可见！
     */
    private boolean deleted = false;

    @Override
    public int hashCode() {
        return Objects.hash(id, store, item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreItem)) return false;
        StoreItem storeItem = (StoreItem) o;
        return Objects.equals(id, storeItem.id) &&
                Objects.equals(store, storeItem.store) &&
                Objects.equals(item, storeItem.item);
    }
}
