package com.agentstudy.rag;

import java.util.List;
import java.util.Optional;

public interface KnowledgeChunkRepository {

    KnowledgeChunk save(KnowledgeChunk chunk);

    Optional<KnowledgeChunk> findById(String id);

    List<KnowledgeChunk> findAll();

    void deleteById(String id);

    int count();
}
