package com.agentstudy.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class InMemoryKnowledgeChunkRepository implements KnowledgeChunkRepository {

    private final ConcurrentMap<String, KnowledgeChunk> chunks = new ConcurrentHashMap<>();

    public InMemoryKnowledgeChunkRepository(InMemoryKnowledgeBase knowledgeBase) {
        knowledgeBase.listChunks().forEach(chunk -> chunks.put(chunk.id(), chunk));
    }

    @Override
    public KnowledgeChunk save(KnowledgeChunk chunk) {
        chunks.put(chunk.id(), chunk);
        return chunk;
    }

    @Override
    public Optional<KnowledgeChunk> findById(String id) {
        return Optional.ofNullable(chunks.get(id));
    }

    @Override
    public List<KnowledgeChunk> findAll() {
        return chunks.values().stream()
                .sorted(Comparator.comparing(KnowledgeChunk::id))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public int count() {
        return chunks.size();
    }
}
