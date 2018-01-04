package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * 平台管理员
 * Created by helloztt on 2017/12/26.
 */
@Entity
@Getter
@Setter
public class Manager {
    @Id
    @ManyToOne
    private Login login;
}
