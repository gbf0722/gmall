package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.MemberException;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @GetMapping("/test")
    public String test(String name){
        System.out.println(name);
        return "ok";
    }

    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String username, @RequestParam("password")String password
            , HttpServletRequest request, HttpServletResponse response){
        String token= this.authService.accredit(username, password);
        //将token放如cookie中
        if (StringUtils.isNotBlank(token)) {
            CookieUtils.setCookie(request,response,this.jwtProperties.getCookieName(),token,
                    this.jwtProperties.getExpire()*60);
            return Resp.ok(null);
        }

        throw new MemberException("用户名或密码错误");


    }



    }
