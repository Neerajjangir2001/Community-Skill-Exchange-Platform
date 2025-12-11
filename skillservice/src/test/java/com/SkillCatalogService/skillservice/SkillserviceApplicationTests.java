package com.SkillCatalogService.skillservice;

import com.SkillCatalogService.skillservice.DTO.SkillRequest;
import com.SkillCatalogService.skillservice.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
class SkillserviceApplicationTests {

//    @Autowired
//    private SkillService skillService;
//
//    @Test
//    public void testBatchInsertPerformance() {
//        // Use YOUR actual user ID from the database
//        UUID userId = UUID.fromString("5b7f6d65-c817-432b-b7fe-0fa8af700448");
//
//        System.out.println("Starting batch insert test...");
//        long start = System.currentTimeMillis();
//
//        for (int i = 0; i < 100; i++) {
//            SkillRequest request = SkillRequest.builder()
//                    .title("Test Skill " + i)
//                    .description("Batch test " + i)
//                    .level("INTERMEDIATE")
//                    .pricePerHour(100.0)
//                    .tags(Arrays.asList("java", "spring", "postgres", "docker", "kafka", "redis"))
//                    .build();
//
//            skillService.createSkill(request, userId);
//        }
//
//        long duration = System.currentTimeMillis() - start;
//
//        System.out.println("================================================");
//        System.out.println("Created 100 skills with 600 tags in: " + duration + "ms");
//        System.out.println("Average per skill: " + (duration / 100) + "ms");
//        System.out.println("================================================");
//    }



}
