package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.UserLevel;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 项目
 * @author helloztt
 */
@Entity
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
     * 会员价
     */
    //TODO 2018/1/12 列如何定义.
    private Map<UserLevel,BigDecimal> vipPrice;
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
    /**
     * 缩略图
     */
    private String thumbnailUrl;
}
