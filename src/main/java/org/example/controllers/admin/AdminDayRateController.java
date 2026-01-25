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
    @Operation(
            summary = "Get all exchange rates with pagination and filtering",
            description = """
                    Retrieves a paginated list of daily exchange rates with optional filtering.
                    
                    **Filtering Options:**
                    - baseCurrency: Filter by base currency (e.g., USD, EUR, UAH)
                    - quoteCurrency: Filter by quote currency
                    - rateDate: Filter by specific date or date range
                    - buyRateBetween: Filter by buy rate range
                    - sellRateBetween: Filter by sell rate range
                    
                    **Pagination:**
                    - page: Page number (0-based)
                    - size: Number of records per page
                    - sortBy: Field to sort by (e.g., rateDate, baseCurrency)
                    - sortOrder: asc or desc
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "baseCurrency",
                            description = "Filter by base currency",
                            example = "USD"
                    ),
                    @Parameter(
                            name = "quoteCurrency",
                            description = "Filter by quote currency",
                            example = "EUR"
                    ),
                    @Parameter(
                            name = "rateDate",
                            description = "Filter by specific date (YYYY-MM-DD) or date range (YYYY-MM-DD,YYYY-MM-DD)",
                            example = "2024-12-23"
                    ),
                    @Parameter(
                            name = "buyRateBetween",
                            description = "Filter by buy rate range (min,max)",
                            example = "0.90,1.10"
                    ),
                    @Parameter(
                            name = "sellRateBetween",
                            description = "Filter by sell rate range (min,max)",
                            example = "0.95,1.15"
                    ),
                    @Parameter(
                            name = "page",
                            description = "Page number (0-based)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of rates per page",
                            example = "20"
                    ),
                    @Parameter(
                            name = "sortBy",
                            description = "Field to sort by",
                            example = "rateDate"
                    ),
                    @Parameter(
                            name = "sortOrder",
                            description = "Sort direction (asc or desc)",
                            example = "desc"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exchange rates retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Response",
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "id": 3,
                                          "baseCurrency": "USD",
                                          "quoteCurrency": "EUR",
                                          "rateDate": "2024-12-23",
                                          "buyRate": 0.940000,
                                          "sellRate": 0.970000,
                                          "createdAt": "2024-12-23T10:00:00",
                                          "updatedAt": "2024-12-23T10:00:00"
                                        },
                                        {
                                          "id": 6,
                                          "baseCurrency": "EUR",
                                          "quoteCurrency": "USD",
                                          "rateDate": "2024-12-23",
                                          "buyRate": 1.030000,
                                          "sellRate": 1.060000,
                                          "createdAt": "2024-12-23T10:00:00",
                                          "updatedAt": "2024-12-23T10:00:00"
                                        }
                                      ],
                                      "pageNumber": 0,
                                      "pageSize": 20,
                                      "totalElements": 10,
                                      "totalPages": 1
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required"
            )
    })
    public PageDto<DayRateReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/day-rates - Filters: {}", filters);

        return dayRateService.getAll(filters);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get exchange rate by ID",
            description = """
                    Retrieves a specific exchange rate by its unique ID.
                    
                    Returns detailed information about the rate including:
                    - Currency pair (base and quote)
                    - Buy and sell rates
                    - Date the rate applies to
                    - Creation and update timestamps
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Exchange rate ID",
                            example = "3",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exchange rate found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 3,
                                      "baseCurrency": "USD",
                                      "quoteCurrency": "EUR",
                                      "rateDate": "2024-12-23",
                                      "buyRate": 0.940000,
                                      "sellRate": 0.970000,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exchange rate not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "DayRate with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Failed to convert value 'abc' to required type 'Integer'",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/abc"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<DayRateReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/day-rates/{}", id);

        DayRateReadDto dayRate = dayRateService.getById(id);
        return ResponseEntity.ok(dayRate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new exchange rate",
            description = """
                    Creates a new daily exchange rate for a currency pair.
                    
                    **Important Business Rules:**
                    - Buy rate must be lower than sell rate (office profit margin)
                    - Only one rate per currency pair per date (unique constraint)
                    - Rate date cannot be in the past (optional validation)
                    - Currencies must be valid (USD, EUR, UAH)
                    - Base currency and quote currency must be different
                    
                    **Rate Convention:**
                    - Buy Rate: Rate at which office BUYS base currency (lower, less favorable to customer)
                    - Sell Rate: Rate at which office SELLS base currency (higher, more favorable to office)
                    - Spread (sell - buy) represents office's profit margin
                    
                    **Example:**
                    USD/EUR with buyRate=0.94, sellRate=0.97
                    - Office buys 1 USD from customer for 0.94 EUR
                    - Office sells 1 USD to customer for 0.97 EUR
                    - Profit: 0.03 EUR per USD
                    
                    Requires ADMIN role.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Exchange rate creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DayRateCreateDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "USD/EUR Rate",
                                            summary = "Create USD to EUR exchange rate",
                                            description = "Office buys USD at 0.94 EUR, sells USD at 0.97 EUR",
                                            value = """
                                            {
                                              "baseCurrency": "USD",
                                              "quoteCurrency": "EUR",
                                              "rateDate": "2024-12-23",
                                              "buyRate": 0.94,
                                              "sellRate": 0.97
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "EUR/UAH Rate",
                                            summary = "Create EUR to UAH exchange rate",
                                            description = "Office buys EUR at 43.80 UAH, sells EUR at 44.30 UAH",
                                            value = """
                                            {
                                              "baseCurrency": "EUR",
                                              "quoteCurrency": "UAH",
                                              "rateDate": "2024-12-23",
                                              "buyRate": 43.80,
                                              "sellRate": 44.30
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "USD/UAH Rate",
                                            summary = "Create USD to UAH exchange rate",
                                            description = "Office buys USD at 41.20 UAH, sells USD at 41.70 UAH",
                                            value = """
                                            {
                                              "baseCurrency": "USD",
                                              "quoteCurrency": "UAH",
                                              "rateDate": "2024-12-23",
                                              "buyRate": 41.20,
                                              "sellRate": 41.70
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
                    description = "Exchange rate created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 11,
                                      "baseCurrency": "USD",
                                      "quoteCurrency": "EUR",
                                      "rateDate": "2024-12-23",
                                      "buyRate": 0.940000,
                                      "sellRate": 0.970000,
                                      "createdAt": "2024-12-23T10:30:00",
                                      "updatedAt": "2024-12-23T10:30:00"
                                    }
                                    """
                            )
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
                                                "baseCurrency": "must not be null",
                                                "buyRate": "must not be null"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/rates"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Rate Values",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Sell rate must be greater than buy rate",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/rates"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Same Currency",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Base currency and quote currency must be different",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/rates"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Rate already exists for this currency pair and date",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Exchange rate already exists for USD/EUR on 2024-12-23",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
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
    @Operation(
            summary = "Update existing exchange rate",
            description = """
                    Updates an existing exchange rate's buy and sell rates.
                    
                    **Updatable Fields:**
                    - buyRate: New buy rate value
                    - sellRate: New sell rate value
                    
                    **Non-Updatable Fields:**
                    - baseCurrency: Cannot be changed (would be a different rate)
                    - quoteCurrency: Cannot be changed (would be a different rate)
                    - rateDate: Cannot be changed (create new rate instead)
                    
                    **Validation:**
                    - Sell rate must still be greater than buy rate
                    - Rates must be positive values
                    
                    **Use Cases:**
                    - Correct data entry errors
                    - Adjust rates based on market changes
                    - Update spread margins
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Exchange rate ID to update",
                            example = "3",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated rate values (all fields optional, but at least one should be provided)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DayRateUpdateDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Update Both Rates",
                                            summary = "Update both buy and sell rates",
                                            value = """
                                            {
                                              "buyRate": 0.95,
                                              "sellRate": 0.98
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Update Buy Rate Only",
                                            summary = "Update only buy rate",
                                            value = """
                                            {
                                              "buyRate": 0.93
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Update Sell Rate Only",
                                            summary = "Update only sell rate",
                                            value = """
                                            {
                                              "sellRate": 0.99
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
                    description = "Exchange rate updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 3,
                                      "baseCurrency": "USD",
                                      "quoteCurrency": "EUR",
                                      "rateDate": "2024-12-23",
                                      "buyRate": 0.950000,
                                      "sellRate": 0.980000,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:35:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exchange rate not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "DayRate with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Rate Spread",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Sell rate must be greater than buy rate",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/rates/3"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Negative Rate",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Rate must be positive",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/rates/3"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
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
    @Operation(
            summary = "Delete exchange rate",
            description = """
                    Deletes an exchange rate by ID.
                    
                    **Important Considerations:**
                    - This will prevent new deals from using this rate
                    - Existing deals that reference this rate may be affected
                    - Consider business impact before deletion
                    
                    **Recommended Approach:**
                    Instead of deleting historical rates:
                    - Create new rates for future dates
                    - Keep historical rates for audit purposes
                    - Only delete rates created in error
                    
                    **Use Cases:**
                    - Remove duplicate rates
                    - Delete rates created with wrong data
                    - Clean up test data
                    
                    Use with caution. Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Exchange rate ID to delete",
                            example = "11",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Exchange rate deleted successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exchange rate not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "DayRate with ID 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Cannot delete rate referenced by existing deals",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Cannot delete rate: referenced by 5 active deals",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/3"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Failed to convert value 'abc' to required type 'Integer'",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/rates/abc"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.warn("DELETE /admin/day-rates/{} - Deleting day rate", id);
        dayRateService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
