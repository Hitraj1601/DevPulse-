package com.devpulse.util;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            boolean available = DockerClientFactory.instance().isDockerAvailable();
            if (available) {
                return ConditionEvaluationResult.enabled("Docker daemon is available for Testcontainers.");
            } else {
                return ConditionEvaluationResult.disabled("Docker daemon is offline. Skipping Testcontainers integration test.");
            }
        } catch (Throwable t) {
            return ConditionEvaluationResult.disabled("Docker daemon not reachable: " + t.getMessage());
        }
    }
}
