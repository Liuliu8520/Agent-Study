package com.agentstudy.agent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "agent-study.llm", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {

    private static final String MODEL_NAME = "mock-llm-v1";

    @Override
    public LlmResponse complete(LlmRequest request) {
        return completeMock(request);
    }

    public static LlmResponse completeMock(LlmRequest request) {
        String content = switch (request.agentType()) {
            case DIAGNOSTICIAN -> "【Mock诊断】已识别学生薄弱点，并给出下一步补强建议。";
            case PLANNER -> "【Mock规划】已生成 3 天递进式学习计划，覆盖概念复习、例题拆解和表达式练习。";
            case LESSON_GENERATOR -> "【Mock讲义】已基于检索知识切片生成 Markdown 微讲义，包含目标、核心知识和易错点。";
            case EXERCISE_GENERATOR -> "【Mock练习】已生成可表达式判卷的练习题，并附带标准答案表达式。";
            case REVIEWER -> "【Mock复习】已根据错误率生成复习建议，必要时安排变式训练。";
        };
        String decoratedContent = content + "\n\npromptCode=" + request.promptCode() + ", sessionId=" + request.sessionId();
        return new LlmResponse(
                MODEL_NAME,
                decoratedContent,
                estimateTokens(request.systemPrompt()) + estimateTokens(request.userPrompt()),
                estimateTokens(decoratedContent)
        );
    }

    private static int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 2);
    }
}
