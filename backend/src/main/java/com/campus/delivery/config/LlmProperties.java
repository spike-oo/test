package com.campus.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型相关配置（对应 application.yml 的 {@code campus.ai}）。
 *
 * <p>API Key 只存服务端，禁止下发前端（需求文档 6.2 大模型安全）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "campus.ai")
public class LlmProperties {

    /** 是否启用大模型能力；关闭时所有 AI 接口走降级逻辑 */
    private boolean enabled = true;

    /** 服务商标识，便于后台切换与结果追溯 */
    private String provider = "openai-compatible";

    /** API 地址 */
    private String apiUrl;

    /** API Key（加密存储，不返回前端） */
    private String apiKey;

    /** 模型名称 */
    private String model;

    /** 温度参数 */
    private Double temperature = 0.7D;

    /** 最大输出 Token */
    private Integer maxTokens = 2048;

    /** 请求超时（毫秒） */
    private Integer timeoutMs = 15000;

    /** 多轮对话上下文保留轮数（滑动窗口） */
    private Integer contextRounds = 6;
}
