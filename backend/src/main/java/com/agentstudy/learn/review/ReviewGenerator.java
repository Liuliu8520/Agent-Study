package com.agentstudy.learn.review;

import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewGenerator {

    public ReviewResult generate(List<ExerciseJudgeResult> exerciseResults) {
        if (exerciseResults == null || exerciseResults.isEmpty()) {
            return new ReviewResult(
                    ReviewStatus.REVIEW_SUGGESTED,
                    "还没有可用于复习分析的练习结果。",
                    0,
                    List.of("请先完成随堂练习，再生成复习建议。"),
                    List.of()
            );
        }

        long wrongCount = exerciseResults.stream().filter(result -> !result.correct()).count();
        double errorRate = (double) wrongCount / exerciseResults.size();

        if (wrongCount == 0) {
            return new ReviewResult(
                    ReviewStatus.COMPLETED,
                    "恭喜完成本轮高数学习闭环！本次诊断、讲义学习和练习表现都很稳定。",
                    0,
                    List.of("建议明天用 10 分钟复盘公式和典型题，保持手感。"),
                    List.of()
            );
        }

        if (errorRate >= 0.5) {
            return new ReviewResult(
                    ReviewStatus.NEEDS_REINFORCEMENT,
                    "本次练习错误率较高，建议先完成 2 道同类型变式题再进入下一轮学习。",
                    errorRate,
                    List.of("优先复盘错题对应的公式和解题步骤。", "做变式题时先写出中间推导，不要只写最终答案。"),
                    generateVariantExercises(exerciseResults)
            );
        }

        return new ReviewResult(
                ReviewStatus.REVIEW_SUGGESTED,
                "本次练习整体通过，但仍有局部知识点需要复盘。",
                errorRate,
                List.of("重点查看错误题的判卷详情。", "用 5 分钟重做错题，确认不是计算失误。"),
                List.of()
        );
    }

    private List<VariantExercise> generateVariantExercises(List<ExerciseJudgeResult> exerciseResults) {
        List<VariantExercise> variants = new ArrayList<>();
        for (ExerciseJudgeResult result : exerciseResults) {
            if (result.correct()) {
                continue;
            }
            variants.add(toVariantExercise(result.questionId()));
            if (variants.size() == 2) {
                return variants;
            }
        }
        while (variants.size() < 2) {
            variants.add(defaultVariant(variants.size() + 1));
        }
        return variants;
    }

    private VariantExercise toVariantExercise(String questionId) {
        return switch (questionId) {
            case "exercise-power-derivative" -> new VariantExercise(
                    "variant-power-derivative",
                    "求函数 f(x)=x^4 的导数，答案输入关于 x 的表达式。",
                    "幂函数求导",
                    "4*x^3"
            );
            case "exercise-chain-derivative" -> new VariantExercise(
                    "variant-chain-derivative",
                    "求函数 f(x)=cos(x^2) 的导数，答案输入关于 x 的表达式。",
                    "链式法则",
                    "-2*x*sin(x^2)"
            );
            case "exercise-basic-integral" -> new VariantExercise(
                    "variant-basic-integral",
                    "求不定积分 ∫3x^2 dx 的一个原函数，忽略常数 C。",
                    "不定积分",
                    "x^3"
            );
            default -> defaultVariant(1);
        };
    }

    private VariantExercise defaultVariant(int index) {
        return new VariantExercise(
                "variant-general-" + index,
                "求函数 f(x)=x^2 的导数，答案输入关于 x 的表达式。",
                "基础导数",
                "2*x"
        );
    }
}

