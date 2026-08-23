package com.desco.complaint.dto.request;

import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintUpdateRequest {

    @NotNull(message = "Area is required")
    private Area area;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;
}
