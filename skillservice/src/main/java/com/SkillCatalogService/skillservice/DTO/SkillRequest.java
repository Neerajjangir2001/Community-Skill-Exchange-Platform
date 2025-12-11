package com.SkillCatalogService.skillservice.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SkillRequest {

    @NotBlank
    private String title;
    private String description;
    private List<String> tags;
    private String level;
    private Double pricePerHour;

}
