package com.young.mall.controller;

import com.young.mall.entity.MallUser;
import com.young.mall.redis.RedisService;
import com.young.mall.result.Result;
import com.young.mall.service.MallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
    MallUserService userService;
	
	@Autowired
    RedisService redisService;
	
    @RequestMapping("/info")
    @ResponseBody
    public Result<MallUser> info(Model model, MallUser user) {
        return Result.success(user);
    }
    
}
