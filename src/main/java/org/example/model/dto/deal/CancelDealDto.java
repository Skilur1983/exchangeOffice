package org.example.model.dto.deal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDealDto {

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String cancellationReason;
}
