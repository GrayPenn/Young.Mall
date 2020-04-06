package com.young.mall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class SimpleController {


    @RequestMapping()
    public String test(Model model) {
        model.addAttribute("name", "young");
        return "hello";
    }
}
