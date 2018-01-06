package com.ming.shopping.beauty.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.http.HttpStatus;

/**
 * @author CJ
 */
@Data
public class ApiResult {

    private final Object data;
    @JsonProperty("resultCode")
    private final int code;
    @JsonProperty("resultMsg")
    private final String message;

    public static ApiResult withCodeAndMessage(int code, String message, Object data) {
        return new ApiResult(data, code, message);
    }

    public static ApiResult withCode(int code, Object data) {
        return withCodeAndMessage(code, "ok", data);
    }

    public static ApiResult withOk(Object data) {
        return withCode(HttpStatusCustom.SC_ACCEPTED, data);
    }

    public static ApiResult withOk() {
        return withOk(null);
    }

    public static ApiResult withError(String message) {
        return withCodeAndMessage(HttpStatusCustom.SC_EXPECTATION_FAILED, message, null);
    }

    public static ApiResult withError(int code, String message) {
        return withCodeAndMessage(code, message, null);
    }

}
