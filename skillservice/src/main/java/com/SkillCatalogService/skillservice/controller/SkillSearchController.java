package com.SkillCatalogService.skillservice.controller;

import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.service.SkillSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skills")
public class SkillSearchController {

    private final SkillSearchService searchService;

    @GetMapping("/search")
    public
    ResponseEntity<Map<String, Object>> search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) List<String> tags,
                                      @RequestParam(required = false) String level,
                                      @RequestParam(required = false) Double minPrice,
                                      @RequestParam(required = false) Double maxPrice,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "createdAt") String sort,
                                      @RequestParam(defaultValue = "desc") String direction) {
        List<SkillResponse> results = searchService.search(q, tags, level, minPrice, maxPrice, page, size, sort, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("count", results.size());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }
}
