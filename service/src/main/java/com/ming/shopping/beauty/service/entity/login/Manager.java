package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

/**
 * Created by helloztt on 2017/12/26.
 */
@Entity
@Getter
@Setter
public class Manager extends Login {

    @Override
    public boolean isManageable() {
        return true;
    }
}
