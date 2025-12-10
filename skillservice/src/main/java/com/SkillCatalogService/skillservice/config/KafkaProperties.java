package com.SkillCatalogService.skillservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers;
    private Topic topic;

    @Data
    public static class Topic {
        private String skillEvents;
        private String skillUpdated;
    }
}
