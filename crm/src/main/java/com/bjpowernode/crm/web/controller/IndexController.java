package com.bjpowernode.crm.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    /**
     * 协议://ip:port/项目名称可以省略
     */
    @RequestMapping("/")
    public String index(){//页面跳转
        //配置视图解析器 使用请求转发,可以访问WEB-INF下面的文件
        return "index";
    }
}
