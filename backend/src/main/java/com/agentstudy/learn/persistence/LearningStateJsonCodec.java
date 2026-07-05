package com.agentstudy.learn.persistence;

import com.agentstudy.learn.LearningState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LearningStateJsonCodec {

    private final ObjectMapper objectMapper;

    public LearningStateJsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(LearningState state) {
        try {
            return objectMapper.writeValueAsString(LearningStateDocument.from(state));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize learning state", exception);
        }
    }

    public LearningState decode(String json) {
        try {
            return objectMapper.readValue(json, LearningStateDocument.class).toLearningState();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize learning state", exception);
        }
    }
}
