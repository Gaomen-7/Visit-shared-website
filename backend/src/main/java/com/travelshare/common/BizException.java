package com.travelshare.common;

/**
 * 可预期的业务拒绝：{@code code} 同时作为 HTTP 状态码与响应信封 code（GDD 0.2）。
 *
 * <p>业务校验失败一律 400；未登录 401；无权限 403。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
