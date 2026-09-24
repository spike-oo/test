package com.campus.delivery.modules.ai.client;

import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型统一客户端。主责：成员1（点餐/搜索/推荐）与成员3（客服）。
 *
 * <p>职责：
 * <ol>
 *   <li>统一请求构造（模型、温度、Token 上限）与超时控制；</li>
 *   <li>统一解析 OpenAI 兼容格式的响应，取出 message.content；</li>
 *   <li>统一异常转换，避免把上游错误直接抛给前端。</li>
 * </ol>
 *
 * <p>约定：所有 AI 能力都通过本类访问大模型，业务模块不直接发起 HTTP 调用；
 * API Key 只存在于服务端配置中。
 */
@Slf4j
@Service
public class LlmClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LlmProperties properties;
    private final RestTemplate restTemplate;

    public LlmClient(LlmProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(properties.getTimeoutMs());
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.restTemplate = new RestTemplate(factory);
    }

    /** 大模型能力是否可用（未配置 Key 时业务需走降级逻辑） */
    public boolean available() {
        return properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
    }

    /** 当前模型标识，用于 AI 结果落库时记录 model_version */
    public String modelVersion() {
        return properties.getProvider() + ":" + properties.getModel();
    }

    /**
     * 发起一次对话请求。
     *
     * @param systemPrompt 系统提示词（角色设定、输出格式约束）
     * @param userPrompt   用户提示词（业务数据注入）
     * @return 模型返回的文本内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!available()) {
            throw new BusinessException(60001, "大模型能力未启用或未配置 API Key");
        }

        Map<String, Object> body = new HashMap<>(8);
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", properties.getMaxTokens());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getApiUrl(), new HttpEntity<>(body, headers), String.class);
            return extractContent(response.getBody());
        } catch (Exception e) {
            log.error("大模型调用失败: provider={}, model={}", properties.getProvider(), properties.getModel(), e);
            throw new BusinessException(60002, "AI 服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 要求模型返回 JSON 并解析为 {@link JsonNode}。
     *
     * <p>解析失败时返回 {@code null}，由调用方决定降级策略
     * （需求文档 5.6.3：解析失败时走关键词搜索兜底）。
     */
    public JsonNode chatForJson(String systemPrompt, String userPrompt) {
        String content = chat(systemPrompt, userPrompt);
        String json = extractJson(content);
        if (json == null) {
            log.warn("模型返回内容不是合法 JSON，进入降级逻辑: {}", abbreviate(content));
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            log.warn("模型返回 JSON 解析失败，进入降级逻辑: {}", e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------

    /** 解析 OpenAI 兼容响应：choices[0].message.content */
    private String extractContent(String rawBody) {
        if (!StringUtils.hasText(rawBody)) {
            throw new BusinessException(60003, "大模型返回内容为空");
        }
        try {
            JsonNode root = MAPPER.readTree(rawBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText("");
            }
            // 部分服务商返回 output_text 字段
            if (root.has("output_text")) {
                return root.path("output_text").asText("");
            }
            throw new BusinessException(60003, "无法解析大模型返回结构");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("大模型响应解析失败", e);
            throw new BusinessException(60003, "无法解析大模型返回结构");
        }
    }

    /** 去掉 Markdown 代码块围栏，截取最外层 JSON 对象 */
    public String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            int fenceEnd = text.lastIndexOf("```");
            if (fenceEnd >= 0) {
                text = text.substring(0, fenceEnd);
            }
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 200 ? text : text.substring(0, 200) + "...";
    }
}
