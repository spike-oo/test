package com.campus.delivery.modules.ai.prompt;

import com.campus.delivery.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示词模板管理。主责：成员1（点餐/搜索/推荐）、成员3（客服）。
 *
 * <p>模板的唯一来源是 {@code backend/src/main/resources/prompts/*.md}，
 * 模板名即文件名（不含扩展名），便于非开发成员直接评审与迭代提示词。
 *
 * <p>变量占位符写法：{@code {{变量名}}}，通过 {@link #format(String, Map)} 注入业务数据。
 */
@Slf4j
@Component
public class PromptTemplates {

    private static final String LOCATION_PATTERN = "classpath*:/prompts/*.md";

    private final Map<String, String> templates = new HashMap<>();

    @PostConstruct
    public void load() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(LOCATION_PATTERN);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (!StringUtils.hasText(filename)) {
                    continue;
                }
                String name = filename.substring(0, filename.lastIndexOf('.'));
                templates.put(name, StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8));
            }
            log.info("提示词模板加载完成，共 {} 个：{}", templates.size(), templates.keySet());
        } catch (IOException e) {
            log.error("提示词模板加载失败", e);
        }
    }

    /** 取模板原文 */
    public String get(String name) {
        String template = templates.get(name);
        if (!StringUtils.hasText(template)) {
            throw new BusinessException(60010, "提示词模板不存在：" + name);
        }
        return template;
    }

    /** 按 {@code {{key}}} 注入变量；未提供的变量保留占位符，便于排查 */
    public String format(String name, Map<String, String> variables) {
        String template = get(name);
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace("{{" + entry.getKey() + "}}", value);
        }
        return result;
    }

    /** 已加载的模板名 */
    public java.util.Set<String> names() {
        return templates.keySet();
    }

    /**
     * 取模板并拆分为 system / user 两段提示词。
     *
     * <p>模板约定：用一行 {@code ## 用户输入} 分隔，
     * 之前是系统提示词（角色设定、输出格式约束），之后是用户提示词（业务数据注入位置）。
     *
     * @return 长度固定为 2 的数组：[0] system 提示词，[1] user 提示词
     */
    public String[] systemAndUser(String name, Map<String, String> variables) {
        String full = format(name, variables);
        String[] parts = full.split("##\\s*用户输入", 2);
        String system = parts[0].trim();
        String user = parts.length > 1 ? parts[1].trim() : "";
        return new String[]{system, user};
    }
}
