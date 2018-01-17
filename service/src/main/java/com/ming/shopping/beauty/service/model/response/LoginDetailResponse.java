package com.ming.shopping.beauty.service.model.response;

import lombok.Data;

/**
 * @author lxf
 */
@Data
public class LoginDetailResponse {
    private long loginId;
    private String username;
    private String mobile;
    private boolean enabled;

    public LoginDetailResponse(long loginId, String username, String mobile, boolean enabled) {
        this.loginId = loginId;
        this.username = username;
        this.mobile = mobile;
        this.enabled = enabled;
    }
}
