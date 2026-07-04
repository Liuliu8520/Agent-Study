package com.agentstudy.rag;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InMemoryKnowledgeBase {

    public List<KnowledgeChunk> listChunks() {
        return List.of(
                new KnowledgeChunk(
                        "chunk-limit-basic",
                        "函数极限",
                        "重要极限 sin(x)/x",
                        "当 x 趋近于 0 时，sin(x)/x 的极限为 1。这是处理三角函数极限时最常用的基本结论之一。",
                        List.of("limit", "limit_basic", "极限", "基础极限")
                ),
                new KnowledgeChunk(
                        "chunk-power-rule",
                        "导数与微分",
                        "幂函数求导公式",
                        "若 f(x)=x^n，则 f'(x)=n*x^(n-1)。例如 f(x)=x^3 时，f'(x)=3x^2。",
                        List.of("derivative", "power_rule", "导数", "幂函数求导")
                ),
                new KnowledgeChunk(
                        "chunk-chain-rule",
                        "导数与微分",
                        "链式法则",
                        "复合函数求导需要先对外层函数求导，再乘以内层函数的导数。若 y=f(g(x))，则 y'=f'(g(x))*g'(x)。",
                        List.of("derivative", "chain_rule", "导数", "链式法则")
                ),
                new KnowledgeChunk(
                        "chunk-indefinite-integral",
                        "不定积分",
                        "基本不定积分",
                        "不定积分可以看作求导的逆运算。因为 (x^2)'=2x，所以 ∫2x dx=x^2+C。",
                        List.of("integral", "indefinite_integral", "积分", "不定积分")
                ),
                new KnowledgeChunk(
                        "chunk-definite-integral",
                        "定积分",
                        "牛顿-莱布尼茨公式",
                        "若 F'(x)=f(x)，则 ∫_a^b f(x)dx=F(b)-F(a)。例如 ∫_0^1 2x dx = x^2|_0^1 = 1。",
                        List.of("integral", "definite_integral", "积分", "定积分")
                )
        );
    }
}

