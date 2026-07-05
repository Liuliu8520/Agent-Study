package com.agentstudy.agent;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository {

    List<PromptTemplate> findAll();

    Optional<PromptTemplate> findByCode(String code);

    PromptTemplate save(PromptTemplate template);
}
