package com.campus.delivery.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码工具。
 *
 * <p>密码一律以 BCrypt 加盐哈希存储，禁止明文入库。
 * 生成后的密文可直接写入数据库，或交给 {@code DevDataInitializer} 自动初始化。
 *
 * <p>命令行使用：
 * <pre>
 *   mvn -q compile exec:java -Dexec.mainClass=com.campus.delivery.common.util.PasswordGenerator
 *   mvn -q compile exec:java -Dexec.mainClass=com.campus.delivery.common.util.PasswordGenerator -Dexec.args="自定义密码"
 * </pre>
 */
public final class PasswordGenerator {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordGenerator() {
    }

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        String raw = (args != null && args.length > 0) ? args[0] : "123456";
        String encoded = encode(raw);
        System.out.println("明文密码: " + raw);
        System.out.println("BCrypt密文: " + encoded);
        System.out.println("校验结果: " + matches(raw, encoded));
    }
}
