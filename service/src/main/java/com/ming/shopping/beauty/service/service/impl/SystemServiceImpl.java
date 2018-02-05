package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author helloztt
 */
@Service("systemService")
public class SystemServiceImpl implements SystemService {

    @Autowired
    private Environment environment;

    @Override
    public String toUrl(String uri) {
        return environment.getProperty("shopping.url", "http://localhost") + uri;
    }

    @Override
    public String toMobileUrl(String uri) {
        return environment.getProperty("shopping.mobile.url", "http://localhost") + uri;
    }

    @Override
    public String toDesktopUrl(String uri) {
        return environment.getProperty("shopping.desktop.url", "http://localhost") + uri;
    }
}
