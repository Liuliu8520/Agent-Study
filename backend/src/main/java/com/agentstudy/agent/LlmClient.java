package com.agentstudy.agent;

public interface LlmClient {

    LlmResponse complete(LlmRequest request);
}
