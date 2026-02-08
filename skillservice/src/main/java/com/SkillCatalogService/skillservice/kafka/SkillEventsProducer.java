package com.SkillCatalogService.skillservice.kafka;

import com.SkillCatalogService.skillservice.model.Skill;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillEventsProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    //  Publish CREATE event
    public void publishSkillCreated(Skill skill, String topic) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "SKILL_CREATED");
            event.put("skillId", skill.getId().toString());
            event.put("userId", skill.getUserId().toString());
            event.put("title", skill.getTitle());
            event.put("description", skill.getDescription());
            event.put("tags", skill.getTags());
            event.put("level", skill.getLevel());
            event.put("pricePerHour", skill.getPricePerHour());
            event.put("status", skill.getStatus() != null ? skill.getStatus().name() : null);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, skill.getId().toString(), message);

            throw new RuntimeException(" Published SKILL_CREATED: " + skill.getId());

        } catch (Exception e) {
            System.err.println(" Failed to publish SKILL_CREATED: " + e.getMessage());
        }
    }

    //  Publish UPDATE event
    public void publishSkillUpdate(Skill skill, String topic) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "SKILL_UPDATED");
            event.put("skillId", skill.getId().toString());
            event.put("userId", skill.getUserId().toString());
            event.put("title", skill.getTitle());
            event.put("description", skill.getDescription());
            event.put("tags", skill.getTags());
            event.put("level", skill.getLevel());
            event.put("pricePerHour", skill.getPricePerHour());
            event.put("status", skill.getStatus() != null ? skill.getStatus().name() : null);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, skill.getId().toString(), message);

            new RuntimeException(" Published SKILL_UPDATED: " + skill.getId());

        } catch (Exception e) {

            new RuntimeException(" Failed to publish SKILL_UPDATED: " + e.getMessage());

        }
    }

    //  Publish DELETE event
    public void publishSkillDeleted(UUID skillId, UUID userId, String topic) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "SKILL_DELETED");
            event.put("skillId", skillId.toString());
            event.put("userId", userId.toString());
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, skillId.toString(), message);

            new RuntimeException(" Published SKILL_DELETED: " + skillId);
        } catch (Exception e) {

            new RuntimeException(" Failed to publish SKILL_DELETED: " + e.getMessage());
        }
    }
}
