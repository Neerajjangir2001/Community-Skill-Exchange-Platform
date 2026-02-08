package com.SkillCatalogService.skillservice.service;

import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.InvalidSearchParametersException;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.SearchServiceException;
import com.SkillCatalogService.skillservice.model.Skill;
import com.SkillCatalogService.skillservice.model.SkillStatus;
import com.authService.exceptionHandler.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;




@Service
@Slf4j
@RequiredArgsConstructor
public class SkillSearchService {

    private final OpenSearchClient client;



    @Value("${opensearch.index}")
    private String index;

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public List<SkillResponse> search(String q,
                                      List<String> tags,
                                      String level,
                                      Double minPrice,
                                      Double maxPrice,
                                      int page,
                                      int size,
                                      String sortField,
                                      String sortDirection) {
        try {
            List<Query> must = new ArrayList<>();
            List<Query> filter = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                must.add(Query.of(b -> b.multiMatch(mm -> mm.query(q).fields("title", "description", "tags"))));
            }
            if (tags != null && !tags.isEmpty()) {
                List<Query> tagShould = tags.stream()
                        .map(t -> Query.of(b -> b.match(m -> m.field("tags").query(FieldValue.of(t)))))
                        .collect(Collectors.toList());
                filter.add(Query.of(b -> b.bool(bb -> bb.should(tagShould).minimumShouldMatch("1"))));
            }

            if (level != null && !level.isEmpty()) {
                filter.add(Query.of(b -> b.match(m -> m.field("level").query(FieldValue.of(level)))));
            }

            if (minPrice != null || maxPrice != null) {
                filter.add(Query.of(b -> b.range(r -> {
                    r.field("pricePerHour");
                    if (minPrice != null) r.gte(JsonData.of(minPrice));
                    if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                    return r;
                })));
            }

            BoolQuery.Builder bool = new BoolQuery.Builder();

            if (must.isEmpty() && filter.isEmpty()) {
                return Collections.emptyList();
            }else if (!must.isEmpty()) bool.must(must);

            if (!filter.isEmpty()) bool.filter(filter);

            SearchRequest sr = SearchRequest.of(s -> s
                    .index(index)
                    .query(bool.build()._toQuery())
                    .from(page * size)
                    .size(size)
                    .sort(so -> so.field(f -> f.field(sortField).order("asc".equalsIgnoreCase(sortDirection) ? SortOrder.Asc : SortOrder.Desc)))
            );
            SearchResponse<Skill> resp = client.search(sr, Skill.class);
            if (resp.hits() == null) return Collections.emptyList();

            return resp.hits().hits().stream()
                    .map(h -> {
                        Skill s = h.source();
                        return SkillResponse.builder()
                                .id(s.getId())
                                .userId(s.getUserId())
                                .title(s.getTitle())
                                .description(s.getDescription())
                                .tags(s.getTags())
                                .level(s.getLevel())
                                .pricePerHour(s.getPricePerHour())
                                .status(s.getStatus() != null ? s.getStatus().name() : null)
                                .createdAt(s.getCreatedAt())
                                .updatedAt(s.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("OpenSearch I/O error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("Failed to communicate with search service", e);

        } catch (OpenSearchException e) {
            logger.error("OpenSearch error during search - Status: {}, Reason: {}",
                    e.status(), e.getMessage(), e);
            throw new SearchServiceException("Search query failed: " + e.getMessage(), e);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters: {}", e.getMessage(), e);
            throw new InvalidSearchParametersException("Invalid search parameters: " + e.getMessage(), e);

        } catch (Exception e) {
            logger.error("Unexpected error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("Unexpected error during search", e);
        }
    }






    public List<SkillResponse> getAllSkills(String search, String level) {
        try {
            List<Query> must = new ArrayList<>();
            List<Query> filter = new ArrayList<>();


            // Text search in title field
            if (search != null && !search.isEmpty()) {
                must.add(Query.of(b -> b.multiMatch(mm -> mm
                        .query(search)
                        .fields("title", "description", "tags"))));
            }

            // Level filter
            if (level != null && !level.isEmpty()) {
                filter.add(Query.of(b -> b.match(m -> m.field("level")
                        .query(FieldValue.of(level)))));
            }

            // Build bool query
            BoolQuery.Builder bool = new BoolQuery.Builder();

            if (must.isEmpty() && filter.isEmpty()) {
                return Collections.emptyList();
            } else if (!must.isEmpty()) bool.must(must);

            if (!filter.isEmpty()) bool.filter(filter);

            // Create search request with pagination
            SearchRequest sr = SearchRequest.of(s -> s
                    .index(index)
                    .query(bool.build()._toQuery())
                    .from(0)
                    .size(100)  // Limit results
                    .sort(so -> so.field(f -> f.field("createdAt")
                            .order(SortOrder.Desc)))
            );

            SearchResponse<Skill> resp = client.search(sr, Skill.class);

            if (resp.hits() == null || resp.hits().hits().isEmpty()) {
                return Collections.emptyList();
            }

            return resp.hits().hits().stream()
                    .map(h -> {
                        Skill s = h.source();
                        return SkillResponse.builder()
                                .id(s.getId())
                                .userId(s.getUserId())
                                .title(s.getTitle())
                                .description(s.getDescription())
                                .tags(s.getTags())
                                .level(s.getLevel())
                                .pricePerHour(s.getPricePerHour())
                                .status(s.getStatus() != null ? s.getStatus().name() : null)
                                .createdAt(s.getCreatedAt())
                                .updatedAt(s.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("OpenSearch I/O error: {}", e.getMessage(), e);
            throw new SearchServiceException("Failed to search skills", e);
        } catch (Exception e) {
            logger.error("Unexpected error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("Search failed", e);
        }
    }




    public List<SkillResponse> getSkillsByUserId(UUID userId) {
        try {
            List<Query> filter = new ArrayList<>();

            // Filter by userId
            filter.add(Query.of(b -> b.match(m -> m.field("userId")
                    .query(FieldValue.of(userId.toString())))));

            BoolQuery.Builder bool = new BoolQuery.Builder();
            if (!filter.isEmpty()) bool.filter(filter);

            SearchRequest sr = SearchRequest.of(s -> s
                    .index(index)
                    .query(bool.build()._toQuery())
                    .size(100)  // Adjust based on expected max skills per user
                    .sort(so -> so.field(f -> f.field("createdAt")
                            .order(SortOrder.Desc)))
            );

            SearchResponse<Skill> resp = client.search(sr, Skill.class);

            if (resp.hits() == null || resp.hits().hits().isEmpty()) {
                return Collections.emptyList();
            }

            return resp.hits().hits().stream()
                    .map(h -> {
                        Skill s = h.source();
                        return SkillResponse.builder()
                                .id(s.getId())
                                .userId(s.getUserId())
                                .title(s.getTitle())
                                .description(s.getDescription())
                                .tags(s.getTags())
                                .level(s.getLevel())
                                .pricePerHour(s.getPricePerHour())
                                .status(s.getStatus() != null ? s.getStatus().name() : null)
                                .createdAt(s.getCreatedAt())
                                .updatedAt(s.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("OpenSearch I/O error for userId {}: {}", userId, e.getMessage(), e);
            throw new SearchServiceException("Failed to fetch skills for user", e);
        } catch (Exception e) {
            logger.error("Error fetching skills for userId {}: {}", userId, e.getMessage(), e);
            throw new SearchServiceException("Failed to fetch user skills", e);
        }
    }

    public List<SkillResponse> searchSkillsByQuery(String query) {
        try {
            List<Query> must = new ArrayList<>();

            // Multi-field search with boosting (title is more important)
            if (query != null && !query.isBlank()) {
                must.add(Query.of(b -> b.multiMatch(mm -> mm
                        .query(query)
                        .fields("title^3", "description^2", "tags^1")  // Boost title matches
                        .fuzziness("AUTO")  // Handle typos
                )));
            } else {
                // If no query, return all active skills
                must.add(Query.of(b -> b.matchAll(m -> m)));
            }

            // Only show ACTIVE skills
            List<Query> filter = new ArrayList<>();
            filter.add(Query.of(b -> b.match(m -> m
                    .field("status")
                    .query(FieldValue.of("ACTIVE")))));

            BoolQuery.Builder bool = new BoolQuery.Builder()
                    .must(must)
                    .filter(filter);

            SearchRequest sr = SearchRequest.of(s -> s
                    .index(index)
                    .query(bool.build()._toQuery())
                    .from(0)
                    .size(50)  // Limit results
                    .sort(so -> so.score(sc -> sc.order(SortOrder.Desc)))  // Sort by relevance
            );

            SearchResponse<Skill> resp = client.search(sr, Skill.class);

            if (resp.hits() == null || resp.hits().hits().isEmpty()) {
                return Collections.emptyList();
            }

            return resp.hits().hits().stream()
                    .map(h -> {
                        Skill s = h.source();
                        return SkillResponse.builder()
                                .id(s.getId())
                                .userId(s.getUserId())
                                .title(s.getTitle())
                                .description(s.getDescription())
                                .tags(s.getTags())
                                .level(s.getLevel())
                                .pricePerHour(s.getPricePerHour())
                                .status(s.getStatus() != null ? s.getStatus().name() : null)
                                .createdAt(s.getCreatedAt())
                                .updatedAt(s.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("OpenSearch I/O error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("Failed to search skills", e);
        } catch (Exception e) {
            logger.error("Unexpected error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("Search failed", e);
        }
    }



}





