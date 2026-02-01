package org.example.controllers.admin;

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
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;
import org.example.service.DealService;
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
@RequestMapping("/admin/deals")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - Deals", description = "Admin endpoints for managing currency exchange deals")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminDealController {

    private final DealService dealService;

    @GetMapping
    @Operation(
            summary = "Get all deals with pagination and filtering",
            description = """
                    Retrieves a paginated list of all currency exchange deals.
                    
                    **Deal Types (from Office perspective):**
                    - **BUY**: Office buying currency from customer (uses buyRate - lower)
                    - **SELL**: Office selling currency to customer (uses sellRate - higher)
                    
                    **Deal Statuses:**
                    - **PAUSED**: Deal created but awaiting confirmation/verification
                    - **COMPLETED**: Deal successfully executed, funds transferred
                    - **FAILED**: Deal attempted but failed (e.g., insufficient funds)
                    - **CANCELLED**: Deal cancelled by admin or customer
                    
                    **Filtering Options:**
                    - status: Filter by deal status
                    - dealType: Filter by type (BUY, SELL)
                    - userId: Filter deals where user is seller OR buyer
                    - sellerId: Filter deals by seller only
                    - buyerId: Filter deals by buyer only
                    - sellerCurrency/buyerCurrency: Filter by currencies
                    - amountBetween, date ranges, etc.
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(name = "status", description = "Filter by status", example = "COMPLETED"),
                    @Parameter(name = "dealType", description = "Filter by type", example = "BUY"),
                    @Parameter(name = "userId", description = "Filter deals where user is seller OR buyer", example = "3"),
                    @Parameter(name = "sellerId", description = "Filter by seller ID", example = "3"),
                    @Parameter(name = "buyerId", description = "Filter by buyer ID", example = "1"),
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20")
            }
    )
    @ApiResponse(responseCode = "200", description = "Deals retrieved successfully")
    public PageDto<DealReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/deals - Filters: {}", filters);

        return dealService.getAll(filters);
    }

    @Operation(
            summary = "Get deal by ID",
            description = """
                    Retrieves detailed information about a specific deal including:
                    - Seller and buyer details
                    - Currency pair and amounts
                    - Applied rate and status
                    - Timestamps and reason fields
                    
                    Requires ADMIN role.
                    """,
            parameters = {@Parameter(name = "id", description = "Deal ID", example = "1", required = true)}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deal found"),
            @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DealReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/deals/{}", id);

        DealReadDto deal = dealService.getById(id);
        return ResponseEntity.ok(deal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new currency exchange deal",
            description = """
                    Creates a new currency exchange deal between office and customer.
                    
                    **Deal Creation Process:**
                    1. Validates rate exists for currency pair and date
                    2. Validates seller and buyer roles (one ADMIN, one CUSTOMER)
                    3. Calculates exchange amounts based on rate
                    4. Checks balance sufficiency for both parties
                    5. Creates deal with appropriate status:
                       - **COMPLETED**: If both parties have sufficient balance (funds transferred immediately)
                       - **PAUSED**: If either party has insufficient balance (awaiting funds)
                       - **FAILED**: If execution fails after creation
                    
                    **Deal Types (from Office perspective):**
                    
                    **BUY Deal** - Office buys from customer:
                    - Seller: CUSTOMER (gives currency)
                    - Buyer: ADMIN/Office (receives currency)
                    - Uses: **buyRate** (lower - favorable to office)
                    - Example: Customer sells 500 EUR → Office pays 525 USD (rate 1.05)
                    
                    **SELL Deal** - Office sells to customer:
                    - Seller: ADMIN/Office (gives currency)
                    - Buyer: CUSTOMER (receives currency)
                    - Uses: **sellRate** (higher - favorable to office)
                    - Example: Customer buys 1000 USD → Pays 970 EUR (rate 0.97)
                    
                    **Critical Business Rules:**
                    1. **Role Validation**: Exactly one ADMIN and one CUSTOMER
                       - BUY deal: Buyer MUST be ADMIN
                       - SELL deal: Seller MUST be ADMIN
                    2. **Rate Selection**: Automatic based on dealType
                       - BUY → uses buyRate from DayRate
                       - SELL → uses sellRate from DayRate
                    3. **Amount Specification**: Can provide soldAmount OR purchasedAmount (not both)
                       - System calculates the other amount based on rate
                       - soldAmount = amount seller gives
                       - purchasedAmount = amount buyer receives
                    4. **Balance Check**: Both parties checked for sufficiency
                       - Both sufficient → COMPLETED immediately
                       - One insufficient → PAUSED (waiting for deposit)
                       - Execution failure → FAILED
                    
                    **Validation Rules:**
                    - Exchange rate must exist for currency pair and date
                    - Seller and buyer must be different users (validated by DTO)
                    - Seller and buyer currencies must be different (validated by DTO)
                    - Seller must have balance in sellerCurrency (or creates)
                    - Buyer must have balance in buyerCurrency (or creates)
                    - Amount must be positive
                    
                    **Use Cases:**
                    - Customer exchanges currency at office counter
                    - Pre-create deal awaiting customer deposit
                    - Batch processing multiple exchanges
                    - Record historical transactions
                    
                    Requires ADMIN role.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deal creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealCreateDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "SELL Deal - Office Sells USD",
                                            summary = "Customer buys 1000 USD from office",
                                            description = """
                                                    Office sells 1000 USD to customer SarSmi (ID=3).
                                                    Customer pays in EUR at sell rate 0.97.
                                                    Specify soldAmount (what office gives).
                                                    System calculates purchasedAmount (970 EUR).
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
                                            name = "BUY Deal - Office Buys EUR",
                                            summary = "Customer sells 500 EUR to office",
                                            description = """
                                                    Customer TomBro (ID=4) sells 500 EUR to office.
                                                    Office pays in USD at buy rate 1.05.
                                                    Office is buyer, customer is seller.
                                                    Specify soldAmount (what customer gives).
                                                    System calculates purchasedAmount (525 USD).
                                                    """,
                                            value = """
                                            {
                                              "dealType": "BUY",
                                              "sellerId": 4,
                                              "buyerId": 1,
                                              "sellerCurrency": "EUR",
                                              "buyerCurrency": "USD",
                                              "soldAmount": 500.00,
                                              "purchasedAmount": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "SELL Deal - Specify Purchase Amount",
                                            summary = "Customer wants exactly 100000 UAH",
                                            description = """
                                                    Customer wants to receive exactly 100000 UAH.
                                                    Specify purchasedAmount (what customer gets).
                                                    System calculates soldAmount based on rate.
                                                    Uses EUR/UAH sell rate (e.g., 44.30).
                                                    System calculates: 100000 ÷ 44.30 = 2257.34 EUR needed.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "SELL",
                                              "sellerId": 2,
                                              "buyerId": 5,
                                              "sellerCurrency": "EUR",
                                              "buyerCurrency": "UAH",
                                              "soldAmount": null,
                                              "purchasedAmount": 100000.00
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "BUY Deal - USD to UAH",
                                            summary = "Customer sells USD for UAH",
                                            description = """
                                                    Customer sells 1000 USD to office.
                                                    Office pays in UAH at buy rate (e.g., 41.20).
                                                    System calculates: 1000 × 41.20 = 41200 UAH.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "BUY",
                                              "sellerId": 9,
                                              "buyerId": 2,
                                              "sellerCurrency": "USD",
                                              "buyerCurrency": "UAH",
                                              "soldAmount": 1000.00,
                                              "purchasedAmount": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Pre-created Deal (Insufficient Funds)",
                                            summary = "Create deal before customer deposits",
                                            description = """
                                                    Create deal in advance, will be PAUSED.
                                                    Customer will deposit EUR later.
                                                    Admin can resume when funds available.
                                                    """,
                                            value = """
                                            {
                                              "dealType": "SELL",
                                              "sellerId": 1,
                                              "buyerId": 12,
                                              "sellerCurrency": "USD",
                                              "buyerCurrency": "EUR",
                                              "soldAmount": 5000.00,
                                              "purchasedAmount": null
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Deal created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMPLETED Deal",
                                            summary = "Deal completed immediately (sufficient funds)",
                                            value = """
                                            {
                                              "id": 11,
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
                                              "completedAt": "2024-12-23T10:45:00",
                                              "createdAt": "2024-12-23T10:45:00",
                                              "updatedAt": "2024-12-23T10:45:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "PAUSED Deal",
                                            summary = "Deal paused (insufficient buyer funds)",
                                            value = """
                                            {
                                              "id": 12,
                                              "dealType": "SELL",
                                              "status": "PAUSED",
                                              "seller": {
                                                "id": 1,
                                                "username": "admin_first",
                                                "role": "ADMIN"
                                              },
                                              "buyer": {
                                                "id": 12,
                                                "username": "NewCustomer",
                                                "role": "CUSTOMER"
                                              },
                                              "sellerCurrency": "USD",
                                              "buyerCurrency": "EUR",
                                              "sellerGives": 5000.0000,
                                              "buyerGives": 4850.0000,
                                              "appliedRate": 0.970000,
                                              "rateDate": "2024-12-23",
                                              "statusReason": "Buyer has insufficient balance",
                                              "completedAt": null,
                                              "createdAt": "2024-12-23T10:45:00",
                                              "updatedAt": "2024-12-23T10:45:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "FAILED Deal",
                                            summary = "Deal failed during execution",
                                            value = """
                                            {
                                              "id": 13,
                                              "dealType": "BUY",
                                              "status": "FAILED",
                                              "seller": {
                                                "id": 4,
                                                "username": "TomBro",
                                                "role": "CUSTOMER"
                                              },
                                              "buyer": {
                                                "id": 1,
                                                "username": "admin_first",
                                                "role": "ADMIN"
                                              },
                                              "sellerCurrency": "EUR",
                                              "buyerCurrency": "USD",
                                              "sellerGives": 500.0000,
                                              "buyerGives": 525.0000,
                                              "appliedRate": 1.050000,
                                              "rateDate": "2024-12-23",
                                              "statusReason": "Execution failed: Optimistic lock exception",
                                              "completedAt": null,
                                              "createdAt": "2024-12-23T10:45:00",
                                              "updatedAt": "2024-12-23T10:45:00"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing Required Fields",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "dealType": "Deal type is required",
                                                "sellerId": "Seller's ID is required",
                                                "soldAmount": "Sold amount must be greater than 0"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Same User Validation",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "isDifferentUsers": "Seller and buyer must be different"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Same Currency Validation",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "isDifferentCurrencies": "Seller and buyer currencies must be different"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Role Configuration",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid deal: BUY deal requires ADMIN as buyer. Current buyer role: CUSTOMER",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Both Customers",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid deal: Must have exactly one ADMIN and one CUSTOMER. Found: 2 CUSTOMER, 0 ADMIN",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Amount Format",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "soldAmount": "Sold amount format is invalid"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found or rate not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Seller Not Found",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Seller user with ID: 999 not found",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Buyer Not Found",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Buyer user with ID: 999 not found",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Rate Not Found",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Exchange rate not found for USD/EUR on 2024-12-23",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/deals"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<DealReadDto> createDeal(@Valid @RequestBody DealCreateDto dealCreateDto) {
        log.info("POST /admin/deals - Type: {}, Seller: {}, Buyer: {}, {} {} → {}",
                dealCreateDto.getDealType(),
                dealCreateDto.getSellerId(),
                dealCreateDto.getBuyerId(),
                dealCreateDto.getSoldAmount() != null ? dealCreateDto.getSoldAmount() : dealCreateDto.getPurchasedAmount(),
                dealCreateDto.getSellerCurrency(),
                dealCreateDto.getBuyerCurrency());

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
            summary = "Resume a paused deal",
            description = """
                    Resumes a PAUSED deal, executing the currency exchange and changing status to COMPLETED.
                    
                    **Process:**
                    1. Validates deal is PAUSED
                    2. Executes currency exchange
                    3. Updates status to COMPLETED
                    4. Records resume reason
                    
                    **Common scenarios:** Verification completed, rate confirmed, approval obtained
                    
                    Requires ADMIN role and resume reason for audit trail.
                    """,
            parameters = {@Parameter(name = "id", description = "Deal ID", example = "3", required = true)}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deal resumed successfully"),
            @ApiResponse(responseCode = "400", description = "Deal not in PAUSED status"),
            @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    public ResponseEntity<DealReadDto> resumeDeal(
            @PathVariable Integer id,
            @Valid @RequestBody ResumeDealDto resumeDealDto) {

        log.info("PATCH /admin/deals/{}/resume - Reason: {}", id, resumeDealDto.getResumeReason());

        DealReadDto resumedDeal = dealService.resumeDeal(id, resumeDealDto);
        return ResponseEntity.ok(resumedDeal);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a deal",
            description = """
                    Cancels a deal changing status to CANCELLED.
                    
                    **For PAUSED deals:** Simply updates status
                    **For COMPLETED deals:** Reverses the transaction
                    
                    **Common scenarios:** Customer changed mind, data error, fraud detection, refund request
                    
                    Requires ADMIN role and cancellation reason for audit trail.
                    """,
            parameters = {@Parameter(name = "id", description = "Deal ID", example = "3", required = true)}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deal cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel deal"),
            @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    public ResponseEntity<DealReadDto> cancelDeal(
            @PathVariable Integer id,
            @Valid @RequestBody CancelDealDto cancelDealDto) {

        log.info("PATCH /admin/deals/{}/cancel - Reason: {}", id, cancelDealDto.getCancellationReason());

        DealReadDto cancelledDeal = dealService.cancelDeal(id, cancelDealDto);
        return ResponseEntity.ok(cancelledDeal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a deal (use with caution)",
            description = """
                    Permanently deletes a deal record.
                    
                    ⚠️ **WARNING:** This is permanent deletion. Consider using CANCEL instead to preserve audit trail.
                    
                    **When appropriate:** Test data cleanup, compliance-approved purging
                    **Better alternative:** Cancel the deal instead
                    
                    Requires ADMIN role and extreme caution.
                    """,
            parameters = {@Parameter(name = "id", description = "Deal ID", example = "999", required = true)}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deal deleted"),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete deal")
    })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("DELETE /admin/deals/{}", id);
        dealService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
