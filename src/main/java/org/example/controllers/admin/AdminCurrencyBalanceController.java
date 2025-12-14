package org.example.controllers.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Currency;
import org.example.model.dto.PageDto;
import org.example.model.dto.currencybalance.BalanceDepositDto;
import org.example.model.dto.currencybalance.BalanceWithdrawalDto;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.service.CurrencyBalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/balances")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminCurrencyBalanceController {

    private final CurrencyBalanceService currencyBalanceService;

    @GetMapping
    public PageDto<CurrencyBalanceReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/balances - Filters: {}", filters);

        return currencyBalanceService.getAll(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyBalanceReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/balances/{}", id);

        CurrencyBalanceReadDto balance = currencyBalanceService.getById(id);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CurrencyBalanceReadDto>> getByUserId(@PathVariable Integer userId) {
        log.debug("GET /admin/balances/user/{}", userId);

        List<CurrencyBalanceReadDto> balances = currencyBalanceService.getAllByUserId(userId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/user/{userId}/currency/{currency}")
    public ResponseEntity<CurrencyBalanceReadDto> getByUserAndCurrency(
            @PathVariable Integer userId,
            @PathVariable Currency currency) {
        log.debug("GET /admin/balances/user/{}/currency/{}", userId, currency);

        CurrencyBalanceReadDto balance = currencyBalanceService.getByUserAndCurrency(userId, currency);
        return ResponseEntity.ok(balance);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CurrencyBalanceReadDto> create(@Valid @RequestBody CurrencyBalanceCreateDto currencyBalanceCreateDto) {
        log.info("POST /admin/balances - User: {}, Currency: {}, Amount: {}",
                currencyBalanceCreateDto.getUserId(),
                currencyBalanceCreateDto.getCurrency(),
                currencyBalanceCreateDto.getAmount());

        CurrencyBalanceReadDto createdBalance = currencyBalanceService.create(currencyBalanceCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBalance.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdBalance);
    }

    @PostMapping("/deposit")
    public ResponseEntity<CurrencyBalanceReadDto> deposit(@Valid @RequestBody BalanceDepositDto depositDto) {
        log.info("POST /admin/balances/deposit - User: {}, Currency: {}, Amount: {}{}",
                depositDto.getUserId(),
                depositDto.getCurrency(),
                depositDto.getAmount(),
                depositDto.getDescription() != null ? ", Description: " + depositDto.getDescription() : "");

        CurrencyBalanceReadDto updatedBalance = currencyBalanceService.deposit(depositDto);

        log.info("Deposit successful - New balance: {}", updatedBalance.getAmount());
        return ResponseEntity.ok(updatedBalance);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CurrencyBalanceReadDto> withdraw(@Valid @RequestBody BalanceWithdrawalDto withdrawalDto) {
        log.info("POST /admin/balances/withdraw - User: {}, Currency: {}, Amount: {}{}",
                withdrawalDto.getUserId(),
                withdrawalDto.getCurrency(),
                withdrawalDto.getAmount(),
                withdrawalDto.getDescription() != null ? ", Description: " + withdrawalDto.getDescription() : "");

        CurrencyBalanceReadDto updatedBalance = currencyBalanceService.withdraw(withdrawalDto);

        log.info("Withdrawal successful - New balance: {}", updatedBalance.getAmount());
        return ResponseEntity.ok(updatedBalance);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.warn("DELETE /admin/balances/{} - Deleting currency balance", id);
        currencyBalanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
