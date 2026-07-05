package com.agentstudy.agent;

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
public class InMemoryPromptTemplateRepository implements PromptTemplateRepository {

    private final ConcurrentMap<String, PromptTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public List<PromptTemplate> findAll() {
        return templates.values().stream()
                .sorted(Comparator.comparing(PromptTemplate::code))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public Optional<PromptTemplate> findByCode(String code) {
        return Optional.ofNullable(templates.get(code));
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        templates.put(template.code(), template);
        return template;
    }
}
