package com.agentstudy.agent;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateVersionRepository {

    PromptTemplateVersion saveVersion(PromptTemplate template, String createdBy, boolean active);

    List<PromptTemplateVersion> findByCode(String code);

    Optional<PromptTemplateVersion> findById(String versionId);

    void activate(String code, String versionId);
}
