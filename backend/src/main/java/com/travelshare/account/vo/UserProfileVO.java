package com.travelshare.account.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 个人中心资料视图，也是「刷新页面恢复登录态」的取数口（不另设 /api/auth/me）。
 */
@Data
@AllArgsConstructor
public class UserProfileVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String intro;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
