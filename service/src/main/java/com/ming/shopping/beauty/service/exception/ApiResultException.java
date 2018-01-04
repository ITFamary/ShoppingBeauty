package com.ming.shopping.beauty.service.exception;

import com.ming.shopping.beauty.service.model.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by helloztt on 2018/1/4.
 */
@Data
@AllArgsConstructor
public class ApiResultException extends RuntimeException {
    private ApiResult apiResult;
}
