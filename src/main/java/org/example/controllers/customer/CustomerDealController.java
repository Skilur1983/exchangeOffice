package org.example.controllers.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Get all my deals",
            description = """
                    Retrieves all currency exchange deals where the authenticated customer is a participant.
                    
                    **Automatically Filtered:**
                    Returns only deals where the customer is either the seller OR the buyer.
                    Cannot view deals between other users.
                    
                    **Deal Types (from Office perspective):**
                    - **BUY**: Office buying from customer → Customer is SELLER
                    - **SELL**: Office selling to customer → Customer is BUYER
                    
                    **Deal Statuses:**
                    - **PAUSED**: Created but awaiting confirmation/funds
                    - **COMPLETED**: Successfully executed, funds transferred
                    - **FAILED**: Execution failed
                    - **CANCELLED**: Cancelled by admin or customer
                    
                    **Optional Filters:**
                    - status: Filter by deal status (PAUSED, COMPLETED, FAILED, CANCELLED)
                    - dealType: Filter by type (BUY, SELL)
                    - sellerCurrency: Filter by currency sold
                    - buyerCurrency: Filter by currency bought
                    - createdAfter: Deals created after date
                    - createdBefore: Deals created before date
                    - page: Page number (0-based)
                    - size: Number of deals per page
                    
                    **Use Cases:**
                    - View transaction history
                    - Check deal statuses
                    - Find specific exchanges
                    - Generate personal statements
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Automatically filtered to authenticated user
                    - Cannot view other customers' deals
                    """,
            parameters = {
                    @Parameter(name = "status", description = "Filter by status", example = "COMPLETED"),
                    @Parameter(name = "dealType", description = "Filter by type", example = "BUY"),
                    @Parameter(name = "sellerCurrency", description = "Filter by sold currency", example = "EUR"),
                    @Parameter(name = "buyerCurrency", description = "Filter by bought currency", example = "USD"),
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deals retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "dealType": "SELL",
                                          "status": "COMPLETED",
                                          "seller": {
                                            "id": 1,
                                            "username": "admin_first",
                                            "role": "ADMIN"
                                          },
                                          "buyer": {
                                            "id": 3,
                                            "username": "SarSmi",
                                            "role": "CUSTOMER"
                                          },
                                          "sellerCurrency": "USD",
                                          "buyerCurrency": "EUR",
                                          "sellerGives": 1000.0000,
                                          "buyerGives": 970.0000,
                                          "appliedRate": 0.970000,
                                          "rateDate": "2024-12-23",
                                          "createdAt": "2024-12-23T10:00:00"
                                        }
                                      ],
                                      "pageNumber": 0,
                                      "pageSize": 20,
                                      "totalElements": 5,
                                      "totalPages": 1
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Wrong role - CUSTOMER required")
    })
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
    @Operation(
            summary = "Get my deal by ID",
            description = """
                    Retrieves a specific deal by ID, but only if the authenticated customer is a participant.
                    
                    **Ownership Validation:**
                    Customer must be either the seller OR the buyer of the deal.
                    Returns 403 Forbidden if trying to access someone else's deal.
                    
                    **Returns Complete Deal Information:**
                    - Deal type and status
                    - Seller and buyer details
                    - Currency pair and amounts
                    - Applied exchange rate
                    - Timestamps and status reasons
                    
                    **Use Cases:**
                    - View specific deal details
                    - Check deal status
                    - Verify transaction amounts
                    - Get deal for receipt/confirmation
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Validates customer is participant
                    - Cannot view other customers' deals
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Deal ID",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deal found and customer is participant",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 1,
                                      "dealType": "SELL",
                                      "status": "COMPLETED",
                                      "seller": {
                                        "id": 1,
                                        "username": "admin_first",
                                        "role": "ADMIN"
                                      },
                                      "buyer": {
                                        "id": 3,
                                        "username": "SarSmi",
                                        "role": "CUSTOMER"
                                      },
                                      "sellerCurrency": "USD",
                                      "buyerCurrency": "EUR",
                                      "sellerGives": 1000.0000,
                                      "buyerGives": 970.0000,
                                      "appliedRate": 0.970000,
                                      "rateDate": "2024-12-23",
                                      "statusReason": null,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not a participant in this deal",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "You can only access your own deals",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/deals/5"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Deal not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Deal with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/deals/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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
    @Operation(
            summary = "Create new currency exchange deal",
            description = """
                    Creates a new currency exchange deal with the office.
                    Customer must be a participant (either seller or buyer).
                    
                    **Customer Creates Deal:**
                    Unlike admins who can create deals for anyone, customers can only create deals 
                    where they are involved (either as seller OR buyer).
                    
                    **Deal Types:**
                    
                    **Customer SELLS currency (BUY deal):**
                    - Customer is the SELLER
                    - Office (admin) is the BUYER
                    - Customer gives currency, receives payment
                    - Example: Customer sells 500 EUR, receives 525 USD
                    
                    **Customer BUYS currency (SELL deal):**
                    - Office (admin) is the SELLER
                    - Customer is the BUYER
                    - Customer pays currency, receives currency
                    - Example: Customer buys 1000 USD, pays 970 EUR
                    
                    **Automatic Status:**
                    - COMPLETED: If both parties have sufficient funds
                    - PAUSED: If funds insufficient (customer can deposit and resume)
                    - FAILED: If execution fails
                    
                    **Validation:**
                    - Customer MUST be sellerId OR buyerId
                    - Other party must be ADMIN
                    - Exchange rate must exist for currency pair
                    - Currencies must be different
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Validates customer participation
                    - Cannot create deals for other customers
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deal creation details - customer must be participant",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealCreateDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Customer Sells EUR (BUY deal)",
                                            summary = "Customer sells 500 EUR to office",
                                            description = """
                                                    Customer (SarSmi, ID=3) sells EUR to office.
                                                    Customer is SELLER, office is BUYER.
                                                    Office pays in USD at buy rate.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "BUY",
                                              "sellerId": 3,
                                              "buyerId": 1,
                                              "sellerCurrency": "EUR",
                                              "buyerCurrency": "USD",
                                              "soldAmount": null,
                                              "purchasedAmount": 500.00
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Customer Buys USD (SELL deal)",
                                            summary = "Customer buys 1000 USD from office",
                                            description = """
                                                    Customer (SarSmi, ID=3) buys USD from office.
                                                    Office is SELLER, customer is BUYER.
                                                    Customer pays in EUR at sell rate.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "SELL",
                                              "sellerId": 1,
                                              "buyerId": 3,
                                              "sellerCurrency": "USD",
                                              "buyerCurrency": "EUR",
                                              "soldAmount": 1000.00,
                                              "purchasedAmount": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Customer Buys UAH",
                                            summary = "Customer needs specific UAH amount",
                                            description = """
                                                    Customer wants exactly 50000 UAH.
                                                    System calculates required EUR based on rate.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "SELL",
                                              "sellerId": 1,
                                              "buyerId": 3,
                                              "sellerCurrency": "UAH",
                                              "buyerCurrency": "EUR",
                                              "soldAmount": null,
                                              "purchasedAmount": 50000.00
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deal created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "errors": {
                                        "isDifferentUsers": "Seller and buyer must be different"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Customer not a participant",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "You must be a participant in the deal (seller or buyer)",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/deals"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User or rate not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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
    @Operation(
            summary = "Resume my paused deal",
            description = """
                    Resumes a deal in PAUSED status that the customer is a participant in.
                    
                    **When to Use:**
                    - Deal was created but customer didn't have sufficient funds
                    - Customer has now deposited required funds
                    - Ready to execute the currency exchange
                    
                    **What Happens:**
                    1. Validates deal is in PAUSED status
                    2. Validates customer is a participant
                    3. Checks both parties now have sufficient funds
                    4. Executes the currency exchange
                    5. Updates deal status to COMPLETED
                    6. Records automatic resume reason
                    
                    **Difference from Admin Resume:**
                    - Admin resume requires explicit reason (for audit)
                    - Customer resume uses automatic reason (self-service)
                    - Both perform same underlying operation
                    
                    **Common Scenario:**
                    1. Customer creates deal (status: PAUSED - insufficient funds)
                    2. Customer deposits funds to balance
                    3. Customer resumes deal (status: COMPLETED)
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Validates ownership (customer must be participant)
                    - Cannot resume other customers' deals
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Deal ID to resume",
                            required = true,
                            example = "3"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deal resumed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 3,
                                      "dealType": "SELL",
                                      "status": "COMPLETED",
                                      "statusReason": null,
                                      "completedAt": "2024-12-23T10:45:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Deal not in PAUSED status",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "message": "Cannot resume deal: Deal is not in PAUSED status. Current: COMPLETED"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not a participant in this deal",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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
    @Operation(
            summary = "Cancel my deal",
            description = """
                    Cancels a deal that the customer is a participant in.
                                        
                    **When to Use:**
                    - Customer changed mind before/after deal execution
                    - Found better exchange rate elsewhere
                    - No longer need the currency
                    - Want refund for completed deal
                                        
                    **What Happens:**
                                        
                    **For PAUSED deals:**
                    - Simply updates status to CANCELLED
                    - No funds were transferred, so no reversal needed
                                        
                    **For COMPLETED deals:**
                    - Updates status to CANCELLED
                    - REVERSES the transaction
                    - Customer gets back what they paid/gave
                    - Acts as a refund
                                        
                    **Cancellation Reason:**
                    Customer must provide reason for audit trail and compliance.
                                        
                    **Examples:**
                    - "Changed mind about exchange"
                    - "Found better rate elsewhere"
                    - "Incorrect amount entered"
                    - "No longer traveling"
                                        
                    **Security:**
                    - Requires CUSTOMER role
                    - Validates ownership (customer must be participant)
                    - Cannot cancel other customers' deals
                    - Reason required for audit compliance
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Deal ID to cancel",
                            required = true,
                            example = "1"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cancellation reason (required)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CancelDealDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Changed Mind",
                                            value = """
                                                    {
                                                      "cancellationReason": "Changed mind about exchange"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Better Rate",
                                            value = """
                                                    {
                                                      "cancellationReason": "Found better exchange rate elsewhere"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Error",
                                            value = """
                                                    {
                                                      "cancellationReason": "Entered wrong amount - should be 1000 not 10000"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deal cancelled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "status": "CANCELLED",
                                              "cancellationReason": "Changed mind about exchange"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or already cancelled",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not a participant in this deal",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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
