package org.example.controllers.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.PageDto;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.dayrate.DayRateUpdateDto;
import org.example.service.DayRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/rates")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - Exchange Rates", description = "Admin endpoints for managing daily exchange rates")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminDayRateController {

    private final DayRateService dayRateService;

    @GetMapping
    public PageDto<DayRateReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/day-rates - Filters: {}", filters);

        return dayRateService.getAll(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DayRateReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/day-rates/{}", id);

        DayRateReadDto dayRate = dayRateService.getById(id);
        return ResponseEntity.ok(dayRate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DayRateReadDto> create(@Valid @RequestBody DayRateCreateDto dayRateCreateDto) {
        log.info("POST /admin/day-rates - Base: {}, Quote: {}, Date: {}",
                dayRateCreateDto.getBaseCurrency(),
                dayRateCreateDto.getQuoteCurrency(),
                dayRateCreateDto.getRateDate());

        DayRateReadDto createdDayRate = dayRateService.create(dayRateCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDayRate.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdDayRate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DayRateReadDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody DayRateUpdateDto dayRateUpdateDto) {

        log.info("PUT /admin/day-rates/{} - Buy: {}, Sell: {}",
                id, dayRateUpdateDto.getBuyRate(), dayRateUpdateDto.getSellRate());

        DayRateReadDto updatedDayRate = dayRateService.update(id, dayRateUpdateDto);

        return ResponseEntity.ok(updatedDayRate);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.warn("DELETE /admin/day-rates/{} - Deleting day rate", id);
        dayRateService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
