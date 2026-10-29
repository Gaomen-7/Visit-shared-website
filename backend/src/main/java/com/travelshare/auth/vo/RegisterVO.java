package com.travelshare.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 注册成功返回的最小视图，前端拿到即可跳登录页。
 */
@Data
@AllArgsConstructor
public class RegisterVO {

    private Long id;
    private String username;
    private String nickname;
}
