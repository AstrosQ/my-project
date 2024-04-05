package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.RestBean;
import org.example.service.AccountDtoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountDtoService service;

    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam String email,
                                        @RequestParam String type,
                                        HttpServletRequest request){
        String message = service.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        if (message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }
}
