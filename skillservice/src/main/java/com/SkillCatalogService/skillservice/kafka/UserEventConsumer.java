package com.SkillCatalogService.skillservice.kafka;

import com.SkillCatalogService.skillservice.event.UserDeletedEvent;
import com.SkillCatalogService.skillservice.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final SkillRepository skillRepository;

    @KafkaListener(topics = "${kafka.topic.user-deleted:user-deleted}", groupId = "skill-service-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for userId: {}", event.getUserId());
        try {
            skillRepository.deleteByUserId(event.getUserId());
            log.info("Deleted skills for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to delete skills for userId: {}", event.getUserId(), e);
        }
    }
}
