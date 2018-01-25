package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.entity.support.UserLevel;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 项目
 *
 * @author helloztt
 */
@Entity
@Getter
@Setter
public class Item implements CrudFriendly<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 商户
     */
    @ManyToOne
    private Merchant merchant;
    /**
     * 缩略图
     */
    @Column(length = 200)
    private String thumbnailUrl;

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
     * 审核状态
     */
    private AuditStatus auditStatus;
    /**
     * 审核备注
     */
    private String auditComment;

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
    private boolean enable = false;
    /**
     * 含义上跟enable完全不同；该值为true 则该货品不会在系统中可见！
     */
    private boolean deleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.getId());
    }
}
