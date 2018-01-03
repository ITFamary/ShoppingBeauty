package com.ming.shopping.beauty.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/manage")
public class ManageIndexController {

    @RequestMapping(value = {"", "/"})
    @ResponseBody
    public String indexTest() {
        return "manage index";
    }
}
