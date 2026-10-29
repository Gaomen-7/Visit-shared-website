package com.travelshare.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功返回体：role 供前端按角色落地，token 供后续请求携带。
 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}
