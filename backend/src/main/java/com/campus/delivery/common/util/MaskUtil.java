package com.campus.delivery.common.util;

/**
 * 敏感信息脱敏工具（需求文档 6.2 数据脱敏）。
 *
 * <p>手机号、身份证号等敏感字段在返回前端前必须调用本工具处理，
 * 原始数据仅限后端内部使用。
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 手机号脱敏：138****0001 */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证号脱敏：1101**********1234 */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /** 姓名脱敏：张* / 张*明 */
    public static String name(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /** 邮箱脱敏：abc***@example.com */
    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 3) {
            return "***" + email.substring(at);
        }
        return email.substring(0, 3) + "***" + email.substring(at);
    }
}
