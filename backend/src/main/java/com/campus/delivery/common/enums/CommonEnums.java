package com.campus.delivery.common.enums;

import lombok.Getter;

/**
 * 通用状态类枚举，避免各模块散落魔法数字。
 */
public final class CommonEnums {

    private CommonEnums() {
    }

    /** 审核状态 */
    @Getter
    public enum AuditStatus {
        WAITING(0, "待审核"),
        PASSED(1, "已通过"),
        REJECTED(2, "已驳回");

        private final Integer code;
        private final String desc;

        AuditStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /** 上下架状态 */
    @Getter
    public enum ShelfStatus {
        OFF(0, "下架"),
        ON(1, "上架");

        private final Integer code;
        private final String desc;

        ShelfStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /** 骑手工作状态 */
    @Getter
    public enum WorkStatus {
        RESTING(0, "休息中"),
        ACCEPTING(1, "接单中");

        private final Integer code;
        private final String desc;

        WorkStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /** 情感倾向（AI 评价分析结果） */
    @Getter
    public enum Sentiment {
        NEGATIVE(0, "差评"),
        NEUTRAL(1, "中评"),
        POSITIVE(2, "好评");

        private final Integer code;
        private final String desc;

        Sentiment(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /** 支付状态 */
    @Getter
    public enum PayStatus {
        UNPAID(0, "未支付"),
        PAID(1, "已支付"),
        REFUNDED(2, "已退款");

        private final Integer code;
        private final String desc;

        PayStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /** 工单状态 */
    @Getter
    public enum TicketStatus {
        WAITING(0, "待处理"),
        PROCESSING(1, "处理中"),
        CLOSED(2, "已关闭");

        private final Integer code;
        private final String desc;

        TicketStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
