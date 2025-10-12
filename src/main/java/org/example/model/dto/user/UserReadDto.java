package org.example.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.RoleName;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReadDto {

    private Integer id;
    private String username;
    private RoleName role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CurrencyBalanceReadDto> currencyBalances;
}
