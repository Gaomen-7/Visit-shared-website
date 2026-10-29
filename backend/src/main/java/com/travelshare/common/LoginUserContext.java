package com.travelshare.common;

/**
 * 请求线程的登录上下文。
 *
 * <p>写入方只有 {@link JwtInterceptor}，读取方是各 Controller/Service。
 * <b>必须由拦截器在请求结束时无条件清除</b>（{@code afterCompletion}），否则线程池复用会把
 * 上一个用户的身份带进下一个请求。
 */
public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 受保护接口读取当前用户；理论上拦截器已保证有值，缺失说明拦截器规则与路由不一致。
     */
    public static LoginUser require() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BizException(401, "未登录或登录状态已失效");
        }
        return loginUser;
    }

    public static Long requireUserId() {
        return require().getUserId();
    }
}
