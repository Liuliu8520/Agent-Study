package com.agentstudy.agent;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class InMemoryPromptTemplateVersionRepository implements PromptTemplateVersionRepository {

    private final ConcurrentMap<String, PromptTemplateVersion> versions = new ConcurrentHashMap<>();

    @Override
    public PromptTemplateVersion saveVersion(PromptTemplate template, String createdBy, boolean active) {
        if (active) {
            deactivateByCode(template.code());
        }
        PromptTemplateVersion version = new PromptTemplateVersion(
                UUID.randomUUID().toString(),
                template.code(),
                template.agentType(),
                template.version(),
                template.name(),
                template.systemPrompt(),
                template.userPromptTemplate(),
                active,
                createdBy,
                Instant.now()
        );
        versions.put(version.versionId(), version);
        return version;
    }

    @Override
    public List<PromptTemplateVersion> findByCode(String code) {
        return versions.values().stream()
                .filter(version -> version.code().equals(code))
                .sorted(Comparator.comparing(PromptTemplateVersion::createdAt).reversed())
                .toList();
    }

    @Override
    public Optional<PromptTemplateVersion> findById(String versionId) {
        return Optional.ofNullable(versions.get(versionId));
    }

    @Override
    public void activate(String code, String versionId) {
        deactivateByCode(code);
        versions.computeIfPresent(versionId, (id, version) -> new PromptTemplateVersion(
                version.versionId(),
                version.code(),
                version.agentType(),
                version.version(),
                version.name(),
                version.systemPrompt(),
                version.userPromptTemplate(),
                true,
                version.createdBy(),
                version.createdAt()
        ));
    }

    private void deactivateByCode(String code) {
        versions.replaceAll((id, version) -> version.code().equals(code)
                ? new PromptTemplateVersion(
                        version.versionId(),
                        version.code(),
                        version.agentType(),
                        version.version(),
                        version.name(),
                        version.systemPrompt(),
                        version.userPromptTemplate(),
                        false,
                        version.createdBy(),
                        version.createdAt()
                )
                : version
        );
    }
}
