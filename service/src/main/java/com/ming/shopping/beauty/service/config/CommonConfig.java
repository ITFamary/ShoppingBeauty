package com.ming.shopping.beauty.service.config;

import com.huotu.verification.VerificationCodeConfig;
import me.jiangcai.lib.jdbc.JdbcSpringConfig;
import me.jiangcai.lib.resource.ResourceSpringConfig;
import me.jiangcai.lib.spring.logging.LoggingConfig;
import me.jiangcai.lib.sys.SystemStringConfig;
import me.jiangcai.lib.thread.ThreadConfig;
import me.jiangcai.wx.WeixinSpringConfig;
import me.jiangcai.wx.standard.StandardWeixinConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by helloztt on 2018/1/5.
 */
@Configuration
@Import({ResourceSpringConfig.class, JdbcSpringConfig.class
        , VerificationCodeConfig.class
        , WeixinSpringConfig.class, StandardWeixinConfig.class
        , ThreadConfig.class
        , SystemStringConfig.class
        , LoggingConfig.class})
public class CommonConfig {
}
