package com.agentstudy.learn.diagnosis;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisQuestionBank {

    public List<DiagnosisQuestion> getDefaultQuestions() {
        return List.of(
                new DiagnosisQuestion(
                        "limit-basic",
                        "limit",
                        "极限",
                        1,
                        "计算 lim_{x->0} sin(x) / x 的值。",
                        List.of(
                                new DiagnosisOption("A", "1"),
                                new DiagnosisOption("B", "0"),
                                new DiagnosisOption("C", "不存在"),
                                new DiagnosisOption("D", "无穷大")
                        ),
                        "A",
                        new WeakPoint("limit_basic", "基础极限", "未掌握 sin(x)/x 在 x->0 时的经典极限")
                ),
                new DiagnosisQuestion(
                        "derivative-power",
                        "derivative",
                        "导数",
                        2,
                        "函数 f(x)=x^3 的导数是？",
                        List.of(
                                new DiagnosisOption("A", "x^2"),
                                new DiagnosisOption("B", "2x"),
                                new DiagnosisOption("C", "3x^2"),
                                new DiagnosisOption("D", "3x")
                        ),
                        "C",
                        new WeakPoint("power_rule", "幂函数求导", "幂函数求导公式 d(x^n)/dx = n*x^(n-1) 掌握不牢")
                ),
                new DiagnosisQuestion(
                        "derivative-chain",
                        "derivative",
                        "导数",
                        3,
                        "函数 f(x)=sin(x^2) 的导数是？",
                        List.of(
                                new DiagnosisOption("A", "cos(x^2)"),
                                new DiagnosisOption("B", "2x*cos(x^2)"),
                                new DiagnosisOption("C", "2x*sin(x^2)"),
                                new DiagnosisOption("D", "cos(2x)")
                        ),
                        "B",
                        new WeakPoint("chain_rule", "链式法则", "复合函数求导时遗漏内层函数的导数")
                ),
                new DiagnosisQuestion(
                        "integral-basic",
                        "integral",
                        "积分",
                        4,
                        "不定积分 ∫ 2x dx 的结果是？",
                        List.of(
                                new DiagnosisOption("A", "2"),
                                new DiagnosisOption("B", "x + C"),
                                new DiagnosisOption("C", "2x + C"),
                                new DiagnosisOption("D", "x^2 + C")
                        ),
                        "D",
                        new WeakPoint("indefinite_integral", "不定积分", "未能把求导规则反向用于基本不定积分")
                ),
                new DiagnosisQuestion(
                        "integral-definite",
                        "integral",
                        "积分",
                        5,
                        "定积分 ∫_0^1 2x dx 的值是？",
                        List.of(
                                new DiagnosisOption("A", "1"),
                                new DiagnosisOption("B", "2"),
                                new DiagnosisOption("C", "0"),
                                new DiagnosisOption("D", "1/2")
                        ),
                        "A",
                        new WeakPoint("definite_integral", "定积分计算", "对定积分上下限代入和牛顿-莱布尼茨公式不熟练")
                )
        );
    }
}

