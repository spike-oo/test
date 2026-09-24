package com.campus.delivery.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.delivery.common.constant.RedisKeys;
import com.campus.delivery.modules.admin.service.TicketService;
import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.dto.AiChatRequest;
import com.campus.delivery.modules.ai.entity.KnowledgeBase;
import com.campus.delivery.modules.ai.mapper.KnowledgeBaseMapper;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AI 智能客服。主责：成员3（需求文档 4.4 / 5.5.4）。
 *
 * <p>流程：
 * <pre>
 *   学生提问 → 维护对话上下文 → 知识库召回 → 判断是否订单相关问题（是则取订单数据）
 *   → 组装提示词调用大模型 → 返回回答；无法回答时生成人工工单
 * </pre>
 *
 * <p>上下文管理：Redis 滑动窗口，只保留最近 N 轮，控制 Token 成本
 * （{@code campus.ai.context-rounds}）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final int KNOWLEDGE_LIMIT = 3;
    private static final int ORDER_CONTEXT_LIMIT = 3;
    private static final long CONTEXT_TTL_MINUTES = 60L;

    /** 订单相关问题关键词，命中时携带订单数据 */
    private static final List<String> ORDER_KEYWORDS =
            List.of("订单", "我的单", "送到", "多久", "配送", "取餐", "退款", "取消", "骑手", "出餐");

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final OrderService orderService;
    private final TicketService ticketService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 多轮对话问答 */
    public Map<String, Object> chat(String userId, AiChatRequest request) {
        String sessionId = StringUtils.hasText(request.getSessionId())
                ? request.getSessionId()
                : UUID.randomUUID().toString().replace("-", "");
        String question = request.getMessage();

        List<String> history = loadContext(sessionId);
        List<KnowledgeBase> knowledge = knowledgeBaseMapper.searchByKeyword(shortKeyword(question), KNOWLEDGE_LIMIT);
        String orderContext = buildOrderContext(userId, question);

        Map<String, Object> result = new HashMap<>(8);
        result.put("sessionId", sessionId);
        result.put("modelVersion", llmClient.modelVersion());

        if (!llmClient.available()) {
            // 降级：直接返回知识库答案，没有则转工单
            if (!knowledge.isEmpty()) {
                result.put("answer", knowledge.get(0).getAnswer());
                result.put("fallback", true);
                result.put("needTicket", false);
            } else {
                result.put("answer", "抱歉，客服暂时无法回答该问题，已为你转人工处理。");
                result.put("fallback", true);
                result.put("needTicket", true);
                createTicket(userId, sessionId, question, null);
            }
            return result;
        }

        Map<String, String> variables = new HashMap<>(6);
        variables.put("knowledge", describeKnowledge(knowledge));
        variables.put("orderContext", orderContext);
        variables.put("history", String.join("\n", history));
        variables.put("message", question);

        String[] prompts = promptTemplates.systemAndUser("chatbot", variables);
        String answer = llmClient.chat(prompts[0], prompts[1]);

        // 模型自述无法解决时转人工工单
        boolean needTicket = answer.contains("[NEED_TICKET]");
        if (needTicket) {
            answer = answer.replace("[NEED_TICKET]", "").trim();
            createTicket(userId, sessionId, question, null);
        }

        appendContext(sessionId, question, answer);
        result.put("answer", answer);
        result.put("fallback", false);
        result.put("needTicket", needTicket);
        result.put("knowledge", knowledge.stream().map(KnowledgeBase::getQuestion).toList());
        return result;
    }

    // ------------------------------------------------------------------

    /** 判断是否订单相关问题 */
    private boolean isOrderRelated(String question) {
        return ORDER_KEYWORDS.stream().anyMatch(question::contains);
    }

    /** 订单相关问题时，带上该学生最近的订单，让模型能给出针对性回答 */
    private String buildOrderContext(String userId, String question) {
        if (!isOrderRelated(question)) {
            return "（本次问题与订单无关）";
        }
        try {
            List<Order> orders = orderService.pageMyOrders(userId, null, 1, ORDER_CONTEXT_LIMIT).getList()
                    .stream().map(vo -> vo.getOrder()).toList();
            if (orders.isEmpty()) {
                return "（该学生暂无历史订单）";
            }
            StringBuilder sb = new StringBuilder();
            for (Order order : orders) {
                sb.append("- 订单号=").append(order.getOrderNo())
                        .append(" | 状态=").append(order.getOrderStatus())
                        .append(" | 金额=").append(order.getTotalAmount())
                        .append(" | 下单时间=").append(order.getCreateTime())
                        .append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("组装订单上下文失败: {}", e.getMessage());
            return "（订单数据暂时不可用）";
        }
    }

    private String describeKnowledge(List<KnowledgeBase> knowledge) {
        if (knowledge.isEmpty()) {
            return "（知识库中没有匹配的问答对）";
        }
        StringBuilder sb = new StringBuilder();
        for (KnowledgeBase item : knowledge) {
            sb.append("Q: ").append(item.getQuestion()).append('\n')
                    .append("A: ").append(item.getAnswer()).append("\n\n");
        }
        return sb.toString();
    }

    /** 取问题的核心关键词（简化处理：去掉常见疑问词后取前 10 个字符） */
    private String shortKeyword(String question) {
        if (!StringUtils.hasText(question)) {
            return "";
        }
        String cleaned = question.replaceAll("[？?！!。，,、\\s]+", "");
        return cleaned.length() <= 10 ? cleaned : cleaned.substring(0, 10);
    }

    private void createTicket(String userId, String sessionId, String question, String orderId) {
        try {
            ticketService.createFromChat(userId, sessionId, orderId, question);
        } catch (Exception e) {
            log.error("工单创建失败", e);
        }
    }

    // ------------------------------------------------------------------
    // 上下文（Redis 滑动窗口）
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<String> loadContext(String sessionId) {
        Object cached = redisTemplate.opsForValue().get(RedisKeys.AI_CHAT_CONTEXT + sessionId);
        if (cached instanceof List) {
            return (List<String>) cached;
        }
        return new ArrayList<>();
    }

    private void appendContext(String sessionId, String question, String answer) {
        List<String> history = loadContext(sessionId);
        history.add("用户：" + question);
        history.add("客服：" + answer);

        // 滑动窗口：只保留最近 6 轮（12 条）
        int maxMessages = 12;
        if (history.size() > maxMessages) {
            history = new ArrayList<>(history.subList(history.size() - maxMessages, history.size()));
        }
        redisTemplate.opsForValue().set(RedisKeys.AI_CHAT_CONTEXT + sessionId, history,
                CONTEXT_TTL_MINUTES, TimeUnit.MINUTES);
        // TODO(成员3)：对话消息落库 ai_chat_session / ai_chat_message，供管理端查看客服对话记录
    }

    /** 清空会话上下文 */
    public void clearContext(String sessionId) {
        redisTemplate.delete(RedisKeys.AI_CHAT_CONTEXT + sessionId);
    }

    /** 知识库列表（管理端维护，见 AdminKnowledgeController） */
    public List<KnowledgeBase> listKnowledge(String category) {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(StringUtils.hasText(category), KnowledgeBase::getCategory, category)
                .orderByDesc(KnowledgeBase::getHitCount));
    }
}
