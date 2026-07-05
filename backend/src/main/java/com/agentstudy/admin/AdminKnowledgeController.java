package com.agentstudy.admin;

import com.agentstudy.admin.audit.OperationLogService;
import com.agentstudy.common.ApiResponse;
import com.agentstudy.common.BusinessException;
import com.agentstudy.rag.KnowledgeChunk;
import com.agentstudy.rag.KnowledgeChunkRepository;
import com.agentstudy.rag.VectorSearchService;
import com.agentstudy.rag.dto.KnowledgeChunkResponse;
import com.agentstudy.rag.dto.RebuildEmbeddingResponse;
import com.agentstudy.rag.dto.UpsertKnowledgeChunkRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rag/chunks")
public class AdminKnowledgeController {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final VectorSearchService vectorSearchService;
    private final OperationLogService operationLogService;

    public AdminKnowledgeController(
            KnowledgeChunkRepository knowledgeChunkRepository,
            VectorSearchService vectorSearchService,
            OperationLogService operationLogService
    ) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.vectorSearchService = vectorSearchService;
        this.operationLogService = operationLogService;
    }

    @PostMapping
    public ApiResponse<KnowledgeChunkResponse> createChunk(
            @Valid @RequestBody UpsertKnowledgeChunkRequest request,
            Principal principal
    ) {
        KnowledgeChunk saved = saveWithEmbedding(request.toChunk(null));
        record(principal, "KNOWLEDGE_CHUNK_CREATE", saved.id(), "Created knowledge chunk");
        return ApiResponse.success(KnowledgeChunkResponse.from(saved));
    }

    @PutMapping("/{chunkId}")
    public ApiResponse<KnowledgeChunkResponse> updateChunk(
            @PathVariable String chunkId,
            @Valid @RequestBody UpsertKnowledgeChunkRequest request,
            Principal principal
    ) {
        requireChunk(chunkId);
        KnowledgeChunk saved = saveWithEmbedding(request.toChunk(chunkId));
        record(principal, "KNOWLEDGE_CHUNK_UPDATE", saved.id(), "Updated knowledge chunk");
        return ApiResponse.success(KnowledgeChunkResponse.from(saved));
    }

    @DeleteMapping("/{chunkId}")
    public ApiResponse<Void> deleteChunk(@PathVariable String chunkId, Principal principal) {
        requireChunk(chunkId);
        knowledgeChunkRepository.deleteById(chunkId);
        record(principal, "KNOWLEDGE_CHUNK_DELETE", chunkId, "Deleted knowledge chunk");
        return ApiResponse.success(null);
    }

    @PostMapping("/{chunkId}/embedding")
    public ApiResponse<RebuildEmbeddingResponse> rebuildEmbedding(@PathVariable String chunkId, Principal principal) {
        KnowledgeChunk chunk = requireChunk(chunkId);
        KnowledgeChunk saved = saveWithEmbedding(chunk);
        record(principal, "KNOWLEDGE_CHUNK_EMBEDDING_REBUILD", chunkId, "Rebuilt knowledge chunk embedding");
        return ApiResponse.success(new RebuildEmbeddingResponse(
                saved.id(),
                saved.hasEmbedding(),
                saved.embedding().size()
        ));
    }

    private KnowledgeChunk saveWithEmbedding(KnowledgeChunk chunk) {
        KnowledgeChunk embedded = chunk.withEmbedding(vectorSearchService.embedChunk(chunk));
        return knowledgeChunkRepository.save(embedded);
    }

    private KnowledgeChunk requireChunk(String chunkId) {
        return knowledgeChunkRepository.findById(chunkId)
                .orElseThrow(() -> BusinessException.notFound("Knowledge chunk not found: " + chunkId));
    }

    private void record(Principal principal, String action, String targetId, String detail) {
        String operator = principal == null ? "system" : principal.getName();
        operationLogService.record(operator, action, "KNOWLEDGE_CHUNK", targetId, detail);
    }
}
