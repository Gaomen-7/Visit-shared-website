package com.travelshare.admin.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelshare.common.LoginUserContext;
import com.travelshare.common.Result;

/**
 * ADMIN 档探针：当前尚无真实管理端接口，需要一个真实路径验证 401 / 403 / 200 三种分叉。
 *
 * <p>后续 admin 模块的真实接口进来后可与之共存或移除。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminProbeController {

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scope", "admin");
        data.put("userId", LoginUserContext.requireUserId());
        data.put("role", LoginUserContext.require().getRole());
        return Result.ok(data);
    }
}
