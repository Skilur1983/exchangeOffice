package org.example.model.dto.deal;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDealDto {

    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String resumeReason;
}
