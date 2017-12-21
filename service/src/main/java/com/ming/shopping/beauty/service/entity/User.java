package com.ming.shopping.beauty.service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by helloztt on 2017/12/21.
 */
@Table(name = "user")
@Entity
@Getter
@Setter
public class User {
    /**
     * 用户编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    /**
     * 用户名
     */
    @Column(name = "uname")
    private String userName;
    /**
     * 密码
     */
    @Column(name = "password")
    private String password;
}
