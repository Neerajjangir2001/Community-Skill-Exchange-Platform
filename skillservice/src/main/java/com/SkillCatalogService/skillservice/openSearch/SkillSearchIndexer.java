package com.SkillCatalogService.skillservice.openSearch;


import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.SkillDeletionException;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.SkillNotFoundException;
import com.SkillCatalogService.skillservice.model.Skill;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SkillSearchIndexer {
    private  final OpenSearchClient openSearchClient;


    @Value("${opensearch.index}")
    private String indexName;

    public void indexSkills(Skill skill)  {
        try {
            openSearchClient.index(i -> i.index(indexName)
                    .id(skill.getId().toString())
                    .document(skill));

        } catch (IOException e) {
            throw new RuntimeException("OpenSearch indexing failed", e);
        }

    }

    public void deleteSkill(UUID skillId) {
        try {
            DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                    .index(indexName)
                    .id(skillId.toString())

            );

            DeleteResponse response = openSearchClient.delete(deleteRequest);

            System.out.println(" Deleted skill from OpenSearch: " + skillId);
            System.out.println("  Result: " + response.result());

        } catch (IOException e) {
            throw new SkillDeletionException("OpenSearch deletion failed for skill: " + skillId, e);

        }
    }
}
