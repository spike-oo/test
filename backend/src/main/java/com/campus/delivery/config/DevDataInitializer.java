package com.campus.delivery.config;

import com.campus.delivery.common.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 开发环境演示账号密码初始化。
 *
 * <p>database/data.sql 中的演示账号不写入伪造的 BCrypt 哈希（密码列为空字符串），
 * 由本类在 dev profile 启动时统一初始化为默认密码 {@link #DEFAULT_PASSWORD} 的密文，
 * 保证「拉下代码 → 建库 → 启动 → 直接登录演示」可用。
 *
 * <p>关闭方式：{@code campus.dev.init-demo-password=false}
 * 或改用 {@code dev,local} 之外的 profile。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final JdbcTemplate jdbcTemplate;

    @Value("${campus.dev.init-demo-password:false}")
    private boolean initDemoPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!initDemoPassword) {
            return;
        }
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE sys_user SET password = ? WHERE (password IS NULL OR password = '') AND deleted = 0",
                    PasswordGenerator.encode(DEFAULT_PASSWORD));
            if (updated > 0) {
                log.warn("已将 {} 个演示账号的密码初始化为默认密码 [{}]，请勿在生产环境开启此功能", updated, DEFAULT_PASSWORD);
            } else {
                log.info("演示账号密码已初始化，跳过");
            }
        } catch (Exception e) {
            log.warn("演示账号密码初始化跳过（可能尚未执行 database/schema.sql）：{}", e.getMessage());
        }
    }
}
