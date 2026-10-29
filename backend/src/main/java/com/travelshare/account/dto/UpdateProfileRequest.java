package com.travelshare.account.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 个人信息全量提交。username 不在入参里：用户名终身不可改（GDD FR-USER-03）。
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过 20 个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Size(max = 200, message = "简介长度不能超过 200 个字符")
    private String intro;
}
