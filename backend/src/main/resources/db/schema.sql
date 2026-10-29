-- 认证与账号模块建表脚本（单一事实源：开发库与测试库执行同一份）
-- 库：travel_share / travel_share_test，字符集 utf8mb4，排序规则 utf8mb4_unicode_ci
-- 注意：username 唯一性不区分大小写依赖 utf8mb4_unicode_ci 的自然行为，不额外写 LOWER() 查重。

CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(20)     NOT NULL COMMENT '用户名，唯一且不区分大小写',
    `password`    VARCHAR(100)    NOT NULL COMMENT 'BCrypt 密文',
    `nickname`    VARCHAR(20)     NOT NULL COMMENT '昵称（不唯一，默认等于用户名）',
    `avatar`      VARCHAR(255)    DEFAULT NULL COMMENT '头像相对 URL',
    `intro`       VARCHAR(200)    DEFAULT NULL COMMENT '个人简介',
    `role`        VARCHAR(10)     NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 禁用',
    `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除旗标，与 status 语义分离',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';
