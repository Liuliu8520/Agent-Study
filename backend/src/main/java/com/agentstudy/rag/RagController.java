package com.agentstudy.rag;

import com.agentstudy.common.ApiResponse;
import com.agentstudy.common.BusinessException;
import com.agentstudy.rag.dto.KnowledgeChunkResponse;
import com.agentstudy.rag.dto.RagRetrieveRequest;
import com.agentstudy.rag.dto.RetrievedKnowledgeChunkResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final RagService ragService;

    public RagController(KnowledgeChunkRepository knowledgeChunkRepository, RagService ragService) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.ragService = ragService;
    }

    @GetMapping("/chunks")
    public ApiResponse<List<KnowledgeChunkResponse>> listChunks() {
        List<KnowledgeChunkResponse> chunks = knowledgeChunkRepository.findAll().stream()
                .map(KnowledgeChunkResponse::from)
                .toList();
        return ApiResponse.success(chunks);
    }

    @GetMapping("/chunks/{chunkId}")
    public ApiResponse<KnowledgeChunkResponse> getChunk(@PathVariable String chunkId) {
        KnowledgeChunk chunk = knowledgeChunkRepository.findById(chunkId)
                .orElseThrow(() -> BusinessException.notFound("Knowledge chunk not found: " + chunkId));
        return ApiResponse.success(KnowledgeChunkResponse.from(chunk));
    }

    @PostMapping("/retrieve")
    public ApiResponse<List<RetrievedKnowledgeChunkResponse>> retrieve(
            @RequestBody(required = false) RagRetrieveRequest request
    ) {
        List<String> keywords = request == null ? List.of() : request.keywords();
        int limit = request == null || request.limit() == null ? 4 : request.limit();
        List<RetrievedKnowledgeChunkResponse> chunks = ragService.retrieveByKeywords(keywords, limit).stream()
                .map(RetrievedKnowledgeChunkResponse::from)
                .toList();
        return ApiResponse.success(chunks);
    }
}
