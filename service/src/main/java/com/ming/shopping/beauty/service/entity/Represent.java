package com.ming.shopping.beauty.service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 门店代表
 * @author lxf
 */
@Entity
@Setter
@Getter
public class Represent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private boolean enable;

    /**
     * 成为门店代表的时间
     */
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdTime;
}
