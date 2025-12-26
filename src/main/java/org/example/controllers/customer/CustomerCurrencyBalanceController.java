package org.example.controllers.customer;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Currency;
import org.example.model.UserPrincipal;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.service.CurrencyBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customer/balances")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer - Balances", description = "Customer endpoints for viewing currency balances")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerCurrencyBalanceController {

    private final CurrencyBalanceService currencyBalanceService;

    @GetMapping
    public ResponseEntity<List<CurrencyBalanceReadDto>> getMyBalances(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/balances - User: {}", userId);

        List<CurrencyBalanceReadDto> balances = currencyBalanceService.getAllByUserId(userId);

        return ResponseEntity.ok(balances);
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<CurrencyBalanceReadDto> getMyBalanceByCurrency(
            @PathVariable Currency currency,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/balances/currency/{} - User: {}", currency, userId);

        CurrencyBalanceReadDto balance = currencyBalanceService.getByUserAndCurrency(userId, currency);

        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyBalanceReadDto> getMyBalanceById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/balances/{} - User: {}", id, userId);

        CurrencyBalanceReadDto balance = currencyBalanceService.getById(id);

        validateOwnership(balance, userId);

        return ResponseEntity.ok(balance);
    }

    private void validateOwnership(CurrencyBalanceReadDto balance, Integer userId) {
        var balanceEntity = currencyBalanceService.getEntityById(balance.getId());

        if (!balanceEntity.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access balance {} belonging to user {} - Access denied",
                    userId, balance.getId(), balanceEntity.getUser().getId());
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only access your own balances");
        }
    }
}
