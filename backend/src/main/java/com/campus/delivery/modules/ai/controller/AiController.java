package com.campus.delivery.modules.ai.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.ai.dto.AiChatRequest;
import com.campus.delivery.modules.ai.dto.AiOrderResponse;
import com.campus.delivery.modules.ai.service.ChatbotService;
import com.campus.delivery.modules.ai.service.DietAnalysisService;
import com.campus.delivery.modules.ai.service.NlOrderService;
import com.campus.delivery.modules.ai.service.RecommendService;
import com.campus.delivery.modules.ai.service.SemanticSearchService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 能力接口（学生端）。主责：成员1（点餐/搜索/推荐/饮食分析）、成员3（智能客服）。
 */
@Tag(name = "10-AI·学生端", description = "AI 点餐、语义搜索、个性化推荐、饮食分析、智能客服")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final NlOrderService nlOrderService;
    private final SemanticSearchService semanticSearchService;
    private final RecommendService recommendService;
    private final DietAnalysisService dietAnalysisService;
    private final ChatbotService chatbotService;

    @Operation(summary = "AI 自然语言点餐", description = "输入「20 块以内、不辣、有肉」这类描述，返回匹配菜品")
    @PostMapping("/order")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<AiOrderResponse> aiOrder(@RequestBody @Valid AiChatRequest request) {
        return Result.ok(nlOrderService.parseAndMatch(UserContext.getUserId(), request));
    }

    @Operation(summary = "语义搜索", description = "理解「想吃点暖和的」这类非标准关键词")
    @PostMapping("/search")
    public Result<AiOrderResponse> semanticSearch(@RequestBody @Valid AiChatRequest request) {
        return Result.ok(semanticSearchService.search(UserContext.getUserId(), request));
    }

    @Operation(summary = "个性化推荐", description = "scene 可选：HOME / AI_ORDER / SEARCH")
    @GetMapping("/recommend")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<Map<String, Object>> recommend(@RequestParam(required = false) String scene,
                                                 @RequestParam(required = false) Integer limit) {
        return Result.ok(recommendService.recommend(UserContext.getUserId(), scene, limit));
    }

    @Operation(summary = "饮食分析报告", description = "days：7 近 7 天 / 30 近 30 天")
    @GetMapping("/diet-report")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<Map<String, Object>> dietReport(@RequestParam(defaultValue = "7") Integer days) {
        return Result.ok(dietAnalysisService.report(UserContext.getUserId(), days));
    }

    @Operation(summary = "智能客服问答", description = "多轮对话；无法回答时返回 needTicket=true 并自动建单")
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody @Valid AiChatRequest request) {
        return Result.ok(chatbotService.chat(UserContext.getUserId(), request));
    }

    @Operation(summary = "清空客服会话上下文")
    @PostMapping("/chat/clear")
    public Result<Void> clearChat(@RequestParam String sessionId) {
        chatbotService.clearContext(sessionId);
        return Result.ok();
    }
}
