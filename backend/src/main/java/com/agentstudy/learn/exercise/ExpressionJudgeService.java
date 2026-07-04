package com.agentstudy.learn.exercise;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
public class ExpressionJudgeService {

    private static final double[] SAMPLE_POINTS = {-2.0, -1.0, -0.5, 0.5, 1.0, 2.0};
    private static final double TOLERANCE = 1e-6;

    public ExerciseJudgeResult judge(ExerciseQuestion question, String studentAnswer) {
        String normalizedAnswer = normalize(studentAnswer);
        if (normalizedAnswer.isEmpty()) {
            return new ExerciseJudgeResult(
                    question.id(),
                    studentAnswer,
                    question.standardAnswer(),
                    false,
                    "Student answer is empty"
            );
        }

        try {
            Expression standardExpression = buildExpression(question.standardAnswer());
            Expression studentExpression = buildExpression(normalizedAnswer);

            for (double x : SAMPLE_POINTS) {
                double expected = standardExpression.setVariable("x", x).evaluate();
                double actual = studentExpression.setVariable("x", x).evaluate();
                if (!Double.isFinite(expected) || !Double.isFinite(actual)) {
                    return incorrect(question, studentAnswer, "Expression produced a non-finite value at x=" + x);
                }
                if (Math.abs(expected - actual) > TOLERANCE) {
                    return incorrect(
                            question,
                            studentAnswer,
                            "Mismatch at x=" + x + ", expected=" + expected + ", actual=" + actual
                    );
                }
            }

            return new ExerciseJudgeResult(
                    question.id(),
                    studentAnswer,
                    question.standardAnswer(),
                    true,
                    "Expression matched on all sample points"
            );
        } catch (IllegalArgumentException exception) {
            return incorrect(question, studentAnswer, "Expression parse error: " + exception.getMessage());
        } catch (ArithmeticException exception) {
            return incorrect(question, studentAnswer, "Expression evaluation error: " + exception.getMessage());
        }
    }

    private Expression buildExpression(String expression) {
        return new ExpressionBuilder(expression)
                .variable("x")
                .build();
    }

    private ExerciseJudgeResult incorrect(ExerciseQuestion question, String studentAnswer, String detail) {
        return new ExerciseJudgeResult(question.id(), studentAnswer, question.standardAnswer(), false, detail);
    }

    private String normalize(String expression) {
        return expression == null ? "" : expression.replace(" ", "").trim();
    }
}

