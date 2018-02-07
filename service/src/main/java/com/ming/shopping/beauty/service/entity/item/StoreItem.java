package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
public class StoreItem implements CrudFriendly<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    @ManyToOne
    private Item item;
    /**
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
    private boolean enable;
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

    public void fromRequest(StoreItem storeItem) {
        setSalesPrice((storeItem.salesPrice));
    }
}
