//package com.SkillCatalogService.skillservice;
//
//import com.SkillCatalogService.skillservice.DTO.SkillRequest;
//import com.SkillCatalogService.skillservice.service.SkillService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Arrays;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping
//public class testcontroler {
//
//    private final SkillService skillService;
//
//    @PostMapping("/test-batch")
//    public ResponseEntity<Map<String, Object>> testBatch(Authentication authentication) {
//        // Use a real user ID that exists in your database
//        UUID userId = UUID.fromString("5b7f6d65-c817-432b-b7fe-0fa8af700448"); // Your actual user
//
//        long start = System.currentTimeMillis();
//
//        for (int i = 0; i < 50; i++) {
//            SkillRequest request = SkillRequest.builder()
//                    .title("Test " + i)
//                    .description("Batch test")
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
//        return ResponseEntity.ok(Map.of(
//                "durationMs", duration,
//                "created", 50,
//                "avgPerSkill", duration / 50 + "ms"
//        ));
//    }
//
//
//}
