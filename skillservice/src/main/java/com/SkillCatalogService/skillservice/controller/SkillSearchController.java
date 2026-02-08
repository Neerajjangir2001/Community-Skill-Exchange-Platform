package com.SkillCatalogService.skillservice.controller;

import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.service.SkillSearchService;
import com.SkillCatalogService.skillservice.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Slf4j
public class SkillSearchController {

    private final SkillSearchService searchService;
    private final SkillService skillService;

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/skillSearch")
    public ResponseEntity<Map<String, Object>> search(@RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        List<SkillResponse> results = searchService.search(q, tags, level, minPrice, maxPrice, page, size, sort,
                direction);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("count", results.size());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllSkills")
    public ResponseEntity<List<SkillResponse>> getAllSkills(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level) {
        log.info("GET /api/search - search: {}, level: {}", search, level);
        return ResponseEntity.ok(skillService.getAllSkills(search, level));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable UUID id) {
        log.info("GET /api/search/{}", id);
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SkillResponse>> getSkillsByUserId(@PathVariable UUID userId) {
        log.info("GET /api/search/user/{}", userId);
        return ResponseEntity.ok(skillService.getSkillsByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(@RequestParam String query) {
        log.info("GET /api/search/search?query={}", query);
        return ResponseEntity.ok(skillService.searchSkills(query));
    }

}
