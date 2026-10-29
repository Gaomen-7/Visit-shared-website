package com.travelshare.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。密钥与有效期均来自配置，换环境只改配置不改代码。
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HS256 签名密钥，长度必须 ≥ 32 字节。 */
    private String secret;

    /** 有效期（小时），SRS 定案 2 小时且无 refresh token。 */
    private long expireHours = 2;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireHours() {
        return expireHours;
    }

    public void setExpireHours(long expireHours) {
        this.expireHours = expireHours;
    }
}
