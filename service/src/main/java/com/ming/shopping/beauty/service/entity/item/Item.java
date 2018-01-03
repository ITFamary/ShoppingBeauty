package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 项目
 * @author helloztt
 */
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"merchant", "code"})})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 项目编码
     */
    @Column(length = 20)
    private String code;
    /**
     * 商户
     */
    @ManyToOne
    private Merchant merchant;

    /**
     * 项目名称
     */
    @Column(length = 40)
    private String name;
    /**
     * 项目类型
     */
    @Column(length = 40)
    private String itemType;

    /**
     * 原价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal price;
    /**
     * 销售价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal salesPrice;
    /**
     * 结算价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal costPrice;

    /**
     * 描述
     */
    @Lob
    private String description;
    /**
     * 富文本描述
     */
    @Lob
    private String richDescription;
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
}
