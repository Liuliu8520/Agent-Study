package com.agentstudy.rag.dto;

import java.util.List;

public record RagRetrieveRequest(
        List<String> keywords,
        Integer limit
) {
}
