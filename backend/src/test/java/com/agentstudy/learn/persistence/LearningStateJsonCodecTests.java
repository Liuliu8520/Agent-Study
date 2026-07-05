package com.agentstudy.learn.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.agentstudy.learn.LearningState;
import com.agentstudy.learn.diagnosis.DiagnosisOption;
import com.agentstudy.learn.diagnosis.DiagnosisQuestion;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.Test;

class LearningStateJsonCodecTests {

    @Test
    void restoresLearningStateFromJsonSnapshot() {
        LearningStateJsonCodec codec = new LearningStateJsonCodec(new ObjectMapper().registerModule(new JavaTimeModule()));
        LearningState state = LearningState.create("session-1", "Alice");
        WeakPoint weakPoint = new WeakPoint("chain_rule", "链式法则", "复合函数求导不熟练");
        state.setDiagnosisQuestions(List.of(new DiagnosisQuestion(
                "q1",
                "derivative",
                "导数",
                2,
                "求 y = sin(x^2) 的导数",
                List.of(new DiagnosisOption("A", "cos(x^2)"), new DiagnosisOption("B", "2xcos(x^2)")),
                "B",
                weakPoint
        )));
        state.setWeakPoints(List.of(weakPoint));
        state.advanceToStep(2);

        LearningState restored = codec.decode(codec.encode(state));

        assertThat(restored.getSessionId()).isEqualTo("session-1");
        assertThat(restored.getStudentName()).isEqualTo("Alice");
        assertThat(restored.getCurrentStep()).isEqualTo(2);
        assertThat(restored.getDiagnosisQuestions()).hasSize(1);
        assertThat(restored.getWeakPoints()).containsExactly(weakPoint);
        assertThat(restored.getUpdatedAt()).isEqualTo(state.getUpdatedAt());
    }
}
