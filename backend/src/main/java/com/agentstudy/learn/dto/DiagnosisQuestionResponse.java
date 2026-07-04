package com.agentstudy.learn.dto;

import com.agentstudy.learn.diagnosis.DiagnosisOption;
import com.agentstudy.learn.diagnosis.DiagnosisQuestion;
import java.util.List;

public record DiagnosisQuestionResponse(
        String id,
        String topicCode,
        String topicName,
        int difficulty,
        String stem,
        List<DiagnosisOption> options
) {

    public static DiagnosisQuestionResponse from(DiagnosisQuestion question) {
        return new DiagnosisQuestionResponse(
                question.id(),
                question.topicCode(),
                question.topicName(),
                question.difficulty(),
                question.stem(),
                question.options()
        );
    }
}

