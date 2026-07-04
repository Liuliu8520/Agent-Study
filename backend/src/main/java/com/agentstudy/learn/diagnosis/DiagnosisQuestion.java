package com.agentstudy.learn.diagnosis;

import java.util.List;

public record DiagnosisQuestion(
        String id,
        String topicCode,
        String topicName,
        int difficulty,
        String stem,
        List<DiagnosisOption> options,
        String correctOptionKey,
        WeakPoint weakPoint
) {

    public boolean isCorrect(String selectedOption) {
        return correctOptionKey.equalsIgnoreCase(selectedOption);
    }
}

