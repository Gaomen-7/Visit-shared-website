package com.travelshare.auth.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travelshare.account.entity.User;
import com.travelshare.account.mapper.UserMapper;
import com.travelshare.auth.dto.LoginRequest;
import com.travelshare.auth.dto.RegisterRequest;
import com.travelshare.auth.vo.LoginVO;
import com.travelshare.auth.vo.RegisterVO;
import com.travelshare.common.BizException;
import com.travelshare.common.JwtUtil;

@Service
public class AuthService {

    private static final String ROLE_USER = "USER";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册。唯一性判定交给数据库（utf8mb4_unicode_ci 天然不区分大小写），
     * 服务端查重只为提前给出友好的 400，不写 LOWER() 造成两套口径。
     */
    @Transactional
    public RegisterVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        long existing = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing > 0) {
            throw new BizException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(username);
        user.setRole(ROLE_USER);
        user.setStatus(1);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发下两个同名注册会同时通过上面的查重，唯一索引在此兜底。
            throw new BizException(400, "用户名已存在");
        }
        return new RegisterVO(user.getId(), user.getUsername(), user.getNickname());
    }

    public LoginVO login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 「用户不存在」与「密码错误」必须同一句提示，避免用户名枚举。
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(400, "该账号已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatar(), user.getRole());
    }
}
