package com.agentstudy.agent;

import com.agentstudy.common.BusinessException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    private final Map<String, PromptTemplate> templates;

    public PromptService() {
        this.templates = loadDefaultTemplates();
    }

    public List<PromptTemplate> listTemplates() {
        return List.copyOf(templates.values());
    }

    public PromptTemplate getTemplate(String code) {
        PromptTemplate template = templates.get(code);
        if (template == null) {
            throw BusinessException.notFound("Prompt template not found: " + code);
        }
        return template;
    }

    public RenderedPrompt render(String code, Map<String, Object> variables) {
        PromptTemplate template = getTemplate(code);
        Map<String, Object> safeVariables = variables == null ? Map.of() : variables;
        return new RenderedPrompt(
                template.code(),
                template.agentType(),
                template.version(),
                renderText(template.systemPrompt(), safeVariables),
                renderText(template.userPromptTemplate(), safeVariables)
        );
    }

    private String renderText(String template, Map<String, Object> variables) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value == null ? "{{" + key + "}}" : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Map<String, PromptTemplate> loadDefaultTemplates() {
        Map<String, PromptTemplate> defaults = new LinkedHashMap<>();
        add(defaults, new PromptTemplate(
                "diagnosis.default",
                AgentType.DIAGNOSTICIAN,
                "v1",
                "高数诊断 Agent",
                "你是高等数学学习诊断 Agent。你需要根据学生答题情况判断薄弱知识点，并给出可解释原因。",
                """
                        学生姓名：{{studentName}}
                        诊断题结果：{{diagnosisResult}}
                        请输出：1. 薄弱点列表；2. 每个薄弱点原因；3. 下一步学习建议。
                        """
        ));
        add(defaults, new PromptTemplate(
                "planner.three-day",
                AgentType.PLANNER,
                "v1",
                "三天学习计划 Agent",
                "你是高数学习规划 Agent。你需要把薄弱点拆成 3 天可执行学习计划。",
                """
                        学生薄弱点：{{weakPoints}}
                        可用学习时间：{{dailyMinutes}} 分钟/天
                        请生成 3 天计划，每天包含目标、概念、练习建议和验收标准。
                        """
        ));
        add(defaults, new PromptTemplate(
                "lesson.micro",
                AgentType.LESSON_GENERATOR,
                "v1",
                "RAG 微讲义 Agent",
                "你是高数微讲义生成 Agent。你必须基于检索到的知识切片组织讲义，不编造不存在的教材内容。",
                """
                        学习计划：{{learningPlan}}
                        薄弱点：{{weakPoints}}
                        检索知识切片：{{retrievedChunks}}
                        请生成一份 Markdown 微讲义，包含学习目标、核心知识、例题、易错点和练习建议。
                        """
        ));
        add(defaults, new PromptTemplate(
                "exercise.generate",
                AgentType.EXERCISE_GENERATOR,
                "v1",
                "练习题生成 Agent",
                "你是高数练习题生成 Agent。你需要围绕讲义生成可自动判卷的表达式题。",
                """
                        微讲义：{{lessonMarkdown}}
                        目标知识点：{{knowledgePoints}}
                        请生成 3 道表达式题，并为每题给出标准答案表达式。
                        """
        ));
        add(defaults, new PromptTemplate(
                "review.feedback",
                AgentType.REVIEWER,
                "v1",
                "智能复习 Agent",
                "你是高数复习反馈 Agent。你需要根据错题表现判断是否结业、复习或强化训练。",
                """
                        练习判卷结果：{{exerciseResults}}
                        错误率：{{errorRate}}
                        请输出复习建议，错误率较高时额外生成 2 道变式题。
                        """
        ));
        return Map.copyOf(defaults);
    }

    private void add(Map<String, PromptTemplate> templates, PromptTemplate template) {
        templates.put(template.code(), template);
    }
}
