package com.agentstudy.learn.lesson;

import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.plan.LearningPlan;
import com.agentstudy.rag.RetrievedKnowledgeChunk;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MicroLessonGenerator {

    private final MicroLessonAgentService microLessonAgentService;

    public MicroLessonGenerator(MicroLessonAgentService microLessonAgentService) {
        this.microLessonAgentService = microLessonAgentService;
    }

    public String generate(
            String sessionId,
            LearningPlan plan,
            List<WeakPoint> weakPoints,
            List<RetrievedKnowledgeChunk> chunks
    ) {
        String agentDraft = microLessonAgentService.generateDraft(sessionId, plan, weakPoints, chunks).outputText();

        StringBuilder markdown = new StringBuilder();
        markdown.append("# 个性化高数微讲义\n\n");
        markdown.append("## 学习目标\n\n");
        markdown.append("- ").append(plan.title()).append("\n");
        if (!weakPoints.isEmpty()) {
            markdown.append("- 本讲义重点补强：")
                    .append(weakPoints.stream().map(WeakPoint::name).distinct().collect(Collectors.joining("、")))
                    .append("\n");
        } else {
            markdown.append("- 本讲义用于巩固极限、导数和积分的基础能力\n");
        }

        markdown.append("\n## Agent 辅助生成摘要\n\n");
        markdown.append(agentDraft).append("\n");

        markdown.append("\n## 核心知识\n\n");
        for (RetrievedKnowledgeChunk chunk : chunks) {
            markdown.append("### ").append(chunk.title()).append("\n\n");
            markdown.append(chunk.content()).append("\n\n");
        }

        markdown.append("## 例题\n\n");
        markdown.append("题目：若 f(x)=sin(x^2)，求 f'(x)。\n\n");
        markdown.append("解析：外层函数是 sin(u)，导数为 cos(u)；内层函数是 u=x^2，导数为 2x。\n\n");
        markdown.append("答案：f'(x)=2x*cos(x^2)。\n\n");

        markdown.append("## 练习建议\n\n");
        plan.days().forEach(day -> {
            markdown.append("- Day ").append(day.day()).append("：").append(day.goal()).append("\n");
            day.practiceSuggestions().forEach(suggestion -> markdown.append("  - ").append(suggestion).append("\n"));
        });

        markdown.append("\n## 引用切片\n\n");
        for (RetrievedKnowledgeChunk chunk : chunks) {
            markdown.append("- ").append(chunk.id()).append("：").append(chunk.chapter()).append(" / ").append(chunk.title()).append("\n");
        }

        return markdown.toString();
    }
}
