package org.example.controllers.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;
import org.example.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/deals")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminDealController {

    private final DealService dealService;

    @GetMapping
    public PageDto<DealReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/deals - Filters: {}", filters);

        return dealService.getAll(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/deals/{}", id);

        DealReadDto deal = dealService.getById(id);
        return ResponseEntity.ok(deal);
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<DealReadDto> resumeDeal(
            @PathVariable Integer id,
            @Valid @RequestBody ResumeDealDto resumeDealDto) {

        log.info("PATCH /admin/deals/{}/resume - Reason: {}", id, resumeDealDto.getResumeReason());

        DealReadDto resumedDeal = dealService.resumeDeal(id, resumeDealDto);
        return ResponseEntity.ok(resumedDeal);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<DealReadDto> cancelDeal(
            @PathVariable Integer id,
            @Valid @RequestBody CancelDealDto cancelDealDto) {

        log.info("PATCH /admin/deals/{}/cancel - Reason: {}", id, cancelDealDto.getCancellationReason());

        DealReadDto cancelledDeal = dealService.cancelDeal(id, cancelDealDto);
        return ResponseEntity.ok(cancelledDeal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("DELETE /admin/deals/{}", id);
        dealService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
