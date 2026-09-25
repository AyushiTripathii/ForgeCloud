package dev.forgecloud.pipeline;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(
    name = "forgecloud.worker.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class PipelineWorkerConfiguration {
}