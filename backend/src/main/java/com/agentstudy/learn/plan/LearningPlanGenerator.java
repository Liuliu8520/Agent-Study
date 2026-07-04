package com.agentstudy.learn.plan;

import com.agentstudy.learn.diagnosis.WeakPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LearningPlanGenerator {

    public LearningPlan generate(List<WeakPoint> weakPoints) {
        List<WeakPoint> effectiveWeakPoints = weakPoints == null ? List.of() : weakPoints;
        if (effectiveWeakPoints.isEmpty()) {
            return generateConsolidationPlan();
        }
        return generateWeakPointPlan(effectiveWeakPoints);
    }

    private LearningPlan generateWeakPointPlan(List<WeakPoint> weakPoints) {
        List<String> weakPointNames = weakPoints.stream()
                .map(WeakPoint::name)
                .distinct()
                .collect(Collectors.toList());
        List<String> conceptNames = new ArrayList<>(weakPointNames);

        return new LearningPlan(
                "3 天高数薄弱点补强计划",
                List.of(
                        new LearningPlanDay(
                                1,
                                "回顾薄弱知识点的定义、公式和适用条件",
                                conceptNames,
                                List.of("整理错题原因", "为每个薄弱点写出 1 个公式和 1 个典型例题")
                        ),
                        new LearningPlanDay(
                                2,
                                "完成针对性例题训练，建立解题步骤",
                                conceptNames,
                                List.of("每个薄弱点完成 2 道基础题", "对比标准解法并标记卡住步骤")
                        ),
                        new LearningPlanDay(
                                3,
                                "进行混合练习和复盘，检查是否能独立完成",
                                conceptNames,
                                List.of("完成 3 道综合题", "总结仍不熟练的公式、条件和常见错误")
                        )
                )
        );
    }

    private LearningPlan generateConsolidationPlan() {
        return new LearningPlan(
                "3 天高数巩固提升计划",
                List.of(
                        new LearningPlanDay(
                                1,
                                "巩固极限、导数、积分的基础定义和常用公式",
                                List.of("基础极限", "幂函数求导", "基本积分"),
                                List.of("复写核心公式", "完成每类知识点 1 道基础题")
                        ),
                        new LearningPlanDay(
                                2,
                                "训练复合函数求导和定积分计算",
                                List.of("链式法则", "定积分计算"),
                                List.of("完成 2 道复合函数求导题", "完成 2 道定积分计算题")
                        ),
                        new LearningPlanDay(
                                3,
                                "进行综合复盘，提升计算稳定性",
                                List.of("极限", "导数", "积分"),
                                List.of("完成 3 道综合练习", "整理一页个人易错清单")
                        )
                )
        );
    }
}

