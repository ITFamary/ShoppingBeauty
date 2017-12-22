package com.ming.shopping.beauty.service.entity;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.util.StringUtils;

import javax.persistence.*;

/**
 * 联系方式
 * @author lxf
 */
@Getter
@Setter
@Entity
public class ContactWay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 移动电话
     */
    @Column(length = 20)
    private String mobile;

    /**
     * 联系人
     */
    @Column(length = 50)
    private String name;

    /**
     * 地址
     */
    private Address address;

    @Override
    public String toString() {
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(mobile))
            return "";
        if (StringUtils.isEmpty(name))
            return mobile;
        if (StringUtils.isEmpty(mobile))
            return name;
        return name + "(" + mobile + ")";
    }
}
