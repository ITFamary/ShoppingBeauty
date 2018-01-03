package com.ming.shopping.beauty.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author helloztt
 */
@Controller
public class IndexController {

    @RequestMapping(value = {"", "/"})
    @ResponseBody
    public String indexTest() {
        return "client index";
    }
}
