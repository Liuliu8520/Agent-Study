package com.agentstudy.learn.exercise;

import com.agentstudy.learn.diagnosis.WeakPoint;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExerciseGenerator {

    public List<ExerciseQuestion> generate(List<WeakPoint> weakPoints, String lessonMarkdown) {
        return List.of(
                new ExerciseQuestion(
                        "exercise-power-derivative",
                        "求函数 f(x)=x^3 的导数，答案输入关于 x 的表达式。",
                        "幂函数求导",
                        "3*x^2"
                ),
                new ExerciseQuestion(
                        "exercise-chain-derivative",
                        "求函数 f(x)=sin(x^2) 的导数，答案输入关于 x 的表达式。",
                        "链式法则",
                        "2*x*cos(x^2)"
                ),
                new ExerciseQuestion(
                        "exercise-basic-integral",
                        "求不定积分 ∫2x dx 的一个原函数，忽略常数 C，答案输入关于 x 的表达式。",
                        "不定积分",
                        "x^2"
                )
        );
    }
}

