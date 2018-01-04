package com.ming.shopping.beauty.service.utils;

import com.huotu.verification.VerificationType;
import me.jiangcai.lib.notice.Content;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * 常量定义
 * Created by helloztt on 2017/12/21.
 */
public class Constant {
    public static final String UTF8_ENCODIND = "UTF-8";

    public static final String DATE_COLUMN_DEFINITION = "timestamp";
    public static final int FLOAT_COLUMN_SCALE = 2;
    public static final int FLOAT_COLUMN_PRECISION = 12;
    /**
     * 银行家舍入发
     */
    public static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;

    /**
     * 短信签名；理论上来讲应该是依赖配置的
     */
    private static final String SMS_SignName = "利每家";

    /**
     * @param code     验证码
     * @param template 模板
     * @return 生成验证码所用的短信内容
     */
    public static Content generateCodeContent(VerificationType type, String code, String template) {
        return new Content() {
            @SuppressWarnings("deprecation")
            @Override
            public String asText() {
                return type.message(code);
            }

            @Override
            public String signName() {
                return Constant.SMS_SignName;
            }

            @Override
            public String templateName() {
                return template;
            }

            @Override
            public Map<String, ?> templateParameters() {
                return Collections.singletonMap("code", code);
            }
        };
    }
}
