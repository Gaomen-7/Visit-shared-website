package com.travelshare.auth.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelshare.auth.dto.LoginRequest;
import com.travelshare.auth.dto.RegisterRequest;
import com.travelshare.auth.service.AuthService;
import com.travelshare.auth.vo.LoginVO;
import com.travelshare.auth.vo.RegisterVO;
import com.travelshare.common.Result;

/**
 * 认证接口（公开档）：注册、登录。登出属于前端清态，不设服务端接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }
}
