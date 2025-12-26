package org.example.controllers.customer;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.UserPrincipal;
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;
import org.example.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/customer/deals")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer - Deals", description = "Customer endpoints for managing deals")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerDealController {

    private final DealService dealService;

    @GetMapping
    public PageDto<DealReadDto> getMyDeals(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Map<String, String> filters) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/deals - User: {}, Filters: {}", userId, filters);

        if (filters == null) {
            filters = new java.util.HashMap<>();
        }
        filters.put("userId", userId.toString());

        return dealService.getAll(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealReadDto> getMyDealById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/deals/{} - User: {}", id, userId);
        DealReadDto deal = dealService.getById(id);

        validateOwnership(deal, userId);

        return ResponseEntity.ok(deal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DealReadDto> createDeal(
            @Valid @RequestBody DealCreateDto dealCreateDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer customerId = userPrincipal.getUser().getId();

        log.info("POST /customer/deals - Customer: {}, DealType: {}, Seller: {}, Buyer: {}",
                customerId, dealCreateDto.getDealType(),
                dealCreateDto.getSellerId(), dealCreateDto.getBuyerId());

        validateCustomerParticipation(dealCreateDto, customerId);
        DealReadDto createdDeal = dealService.createDeal(dealCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDeal.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdDeal);
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<DealReadDto> resumeMyDeal(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();

        log.info("PATCH /customer/deals/{}/resume - User: {}", id, userId);

        DealReadDto deal = dealService.getById(id);
        validateOwnership(deal, userId);

        ResumeDealDto resumeDto = ResumeDealDto.builder().build();
        DealReadDto resumedDeal = dealService.resumeDeal(id, resumeDto);

        return ResponseEntity.ok(resumedDeal);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<DealReadDto> cancelMyDeal(
            @PathVariable Integer id,
            @Valid @RequestBody CancelDealDto cancelDealDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();

        log.info("PATCH /customer/deals/{}/cancel - User: {}, Reason: {}",
                id, userId, cancelDealDto.getCancellationReason());

        DealReadDto deal = dealService.getById(id);
        validateOwnership(deal, userId);

        DealReadDto cancelledDeal = dealService.cancelDeal(id, cancelDealDto);

        return ResponseEntity.ok(cancelledDeal);
    }

    private void validateOwnership(DealReadDto deal, Integer userId) {
        boolean isParticipant = deal.getSeller().getId().equals(userId) || deal.getBuyer().getId().equals(userId);

        if (!isParticipant) {
            log.warn("User {} attempted to access deal {} - Access denied", userId, deal.getId());
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only access your own deals");
        }
    }

    private void validateCustomerParticipation(DealCreateDto dto, Integer customerId) {
        boolean isParticipant = dto.getSellerId().equals(customerId)
                || dto.getBuyerId().equals(customerId);

        if (!isParticipant) {
            log.warn("Customer {} attempted to create deal without being a participant - Seller: {}, Buyer: {}",
                    customerId, dto.getSellerId(), dto.getBuyerId());
            throw new org.springframework.security.access.AccessDeniedException(
                    "You must be a participant in the deal (seller or buyer)");
        }
    }
}
