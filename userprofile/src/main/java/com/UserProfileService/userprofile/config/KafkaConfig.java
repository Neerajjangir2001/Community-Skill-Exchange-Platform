package com.UserProfileService.userprofile.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name(userDeletedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
