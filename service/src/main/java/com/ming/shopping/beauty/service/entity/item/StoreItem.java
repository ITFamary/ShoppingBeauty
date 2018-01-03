package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 门店项目
 * @author helloztt
 */
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"store", "item"})})
public class StoreItem {

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
     * 是否上架
     */
    private boolean enable = true;
}
