package com.ming.shopping.beauty.service.entity.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author helloztt
 */
@AllArgsConstructor
@Getter
public enum  FlowType {
    IN("充值"),
    OUT("订单消费");
    private final String message;

    @Override
    public String toString() {
        return message;
    }
}
