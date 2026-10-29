package com.travelshare.account.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelshare.account.dto.ChangePasswordRequest;
import com.travelshare.account.dto.UpdateProfileRequest;
import com.travelshare.account.service.AccountService;
import com.travelshare.account.vo.UserProfileVO;
import com.travelshare.common.LoginUserContext;
import com.travelshare.common.Result;

/**
 * 账号呈现接口（登录档）：个人资料读写与改密。凭证相关逻辑归 auth，呈现与维护归本模块。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AccountService accountService;

    public UserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.ok(accountService.getProfile(LoginUserContext.requireUserId()));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(accountService.updateProfile(LoginUserContext.requireUserId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(LoginUserContext.requireUserId(), request);
        return Result.ok();
    }
}
