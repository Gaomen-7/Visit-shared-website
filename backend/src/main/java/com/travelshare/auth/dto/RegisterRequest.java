package com.travelshare.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 注册入参。刻意不含 role 字段：管理员仅经角色分配产生，注册一律落 USER。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{3,20}$",
            message = "用户名须为 3–20 个字符，仅限中文、字母、数字或下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度须为 6–20 个字符")
    private String password;
}
