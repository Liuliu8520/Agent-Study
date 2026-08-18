package com.agentstudy.learn;

import static org.assertj.core.api.Assertions.assertThat;

import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import com.agentstudy.learn.exercise.ExerciseQuestion;
import com.agentstudy.learn.exercise.ExpressionJudgeService;
import org.junit.jupiter.api.Test;

class ExpressionJudgeServiceTests {

    private final ExpressionJudgeService expressionJudgeService = new ExpressionJudgeService();

    @Test
    void acceptsNaturalLogAlias() {
        ExerciseQuestion question = new ExerciseQuestion(
                "log-alias",
                "求函数值",
                "natural_log",
                "log(x^2+1)"
        );

        ExerciseJudgeResult result = expressionJudgeService.judge(question, "ln(x^2 + 1)");

        assertThat(result.correct()).isTrue();
    }

    @Test
    void acceptsCommonExponentialAlias() {
        ExerciseQuestion question = new ExerciseQuestion(
                "exp-alias",
                "求函数值",
                "exponential",
                "exp(x)"
        );

        ExerciseJudgeResult result = expressionJudgeService.judge(question, "e^x");

        assertThat(result.correct()).isTrue();
    }
}
