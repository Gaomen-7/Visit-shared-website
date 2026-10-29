package com.travelshare.account.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.travelshare.account.dto.ChangePasswordRequest;
import com.travelshare.account.dto.UpdateProfileRequest;
import com.travelshare.account.entity.User;
import com.travelshare.account.mapper.UserMapper;
import com.travelshare.account.vo.UserProfileVO;
import com.travelshare.common.BizException;

@Service
public class AccountService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileVO getProfile(Long userId) {
        User user = requireUser(userId);
        return new UserProfileVO(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(),
                user.getIntro(), user.getCreateTime());
    }

    /**
     * 全量提交昵称 / 头像 / 简介。username 不在可更新列内，签名上没有入参即「传了也无效」。
     */
    @Transactional
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest request) {
        requireUser(userId);
        String nickname = request.getNickname().trim();
        String intro = request.getIntro() == null ? null : request.getIntro().trim();

        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getNickname, nickname)
                .set(User::getAvatar, request.getAvatar())
                .set(User::getIntro, intro));
        return getProfile(userId);
    }

    /**
     * 改密只更新凭证；在途 token 不强制失效（GDD 0.3 定案，立即失效属 P1 Redis 黑名单）。
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException(400, "旧密码不正确");
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getPassword, passwordEncoder.encode(request.getNewPassword())));
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "未登录或登录状态已失效");
        }
        return user;
    }
}
