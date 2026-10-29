-- 认证与账号模块种子（开发库与测试库执行同一份）
-- 管理员仅经角色分配产生，不另设注册入口。口令为 admin123 的 BCrypt 密文。
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `intro`, `role`, `status`, `deleted`)
VALUES (1, 'admin', '$2a$10$uGxE/nHHi76asx/0lf/wNuGC8SL.Sa2SjvKh2eIjdXqLc2X0/.9jW',
        '管理员', '系统预置管理员账号', 'ADMIN', 1, 0);
