package org.example.controllers.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/customer/balance")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer - Balances", description = "Customer endpoints for viewing currency balances")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerCurrencyBalanceController {

    private final CurrencyBalanceService currencyBalanceService;

    @GetMapping
    @Operation(
            summary = "Get all my currency balances",
            description = """
                    Retrieves all currency balances for the authenticated customer.
                    
                    **Returns:**
                    - List of all currencies the customer has balances in
                    - Empty list if customer has no balances yet
                    - Only balances belonging to the authenticated user
                    
                    **Typical Response:**
                    Customer may have 0-3 balances (one for each: USD, EUR, UAH)
                    
                    **Use Cases:**
                    - View complete portfolio/wallet
                    - Check available funds before creating deal
                    - Display account overview in mobile app
                    - Generate personal financial statement
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Automatically filtered to authenticated user
                    - Cannot view other customers' balances
                    
                    **Note:** No pagination needed - max 3 currencies per customer.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balances retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Customer with Multiple Balances",
                                            summary = "Customer has USD, EUR, and UAH",
                                            value = """
                                            [
                                              {
                                                "id": 1,
                                                "userId": 3,
                                                "username": "SarSmi",
                                                "currency": "USD",
                                                "amount": 5000.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              },
                                              {
                                                "id": 2,
                                                "userId": 3,
                                                "username": "SarSmi",
                                                "currency": "EUR",
                                                "amount": 3000.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              },
                                              {
                                                "id": 3,
                                                "userId": 3,
                                                "username": "SarSmi",
                                                "currency": "UAH",
                                                "amount": 120000.0000,
                                                "version": 2,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T11:30:00"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Customer with Single Balance",
                                            summary = "Customer has only EUR",
                                            value = """
                                            [
                                              {
                                                "id": 9,
                                                "userId": 6,
                                                "username": "MarJon",
                                                "currency": "EUR",
                                                "amount": 10000.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "New Customer with No Balances",
                                            summary = "Customer just registered, no deposits yet",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated - Invalid or missing token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Authentication token has expired",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Wrong role (ADMIN trying to use customer endpoint)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access denied",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<List<CurrencyBalanceReadDto>> getMyBalances(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/balances - User: {}", userId);

        List<CurrencyBalanceReadDto> balances = currencyBalanceService.getAllByUserId(userId);

        return ResponseEntity.ok(balances);
    }

    @GetMapping("/currency/{currency}")
    @Operation(
            summary = "Get my balance for specific currency",
            description = """
                    Retrieves the authenticated customer's balance for a specific currency.
                    
                    **Use Cases:**
                    - Check USD balance before creating exchange deal
                    - Verify EUR balance before withdrawal
                    - Display single currency balance in UI
                    - Quick balance lookup for specific currency
                    
                    **Note:** Returns 404 if customer doesn't have a balance in that currency.
                    Customer only has balances for currencies they've deposited or received in deals.
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Automatically scoped to authenticated user
                    - Cannot check other customers' balances
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 1,
                                      "userId": 3,
                                      "username": "SarSmi",
                                      "currency": "USD",
                                      "amount": 5000.0000,
                                      "version": 0,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Balance not found for this currency",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Balance not found for user 3 and currency UAH",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances/currency/UAH"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid currency code",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid currency: GBP. Allowed: USD, EUR, UAH",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances/currency/GBP"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Wrong role")
    })
    public ResponseEntity<CurrencyBalanceReadDto> getMyBalanceByCurrency(
            @Parameter(
                    description = "Currency code (USD, EUR, or UAH)",
                    required = true,
                    example = "USD"
            )
            @PathVariable Currency currency,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/balances/currency/{} - User: {}", currency, userId);

        CurrencyBalanceReadDto balance = currencyBalanceService.getByUserAndCurrency(userId, currency);

        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get my balance by ID",
            description = """
                    Retrieves a specific balance by ID, but only if it belongs to the authenticated customer.
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Validates balance ownership
                    - Returns 403 if trying to access another customer's balance
                    
                    **Use Cases:**
                    - Get balance details after receiving balance ID from another endpoint
                    - Refresh specific balance information
                    - Verify balance still exists
                    
                    **Note:** Most customers will use GET /customer/balances or 
                    GET /customer/balances/currency/{currency} instead.
                    This endpoint is useful when you have a specific balance ID.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance found and belongs to authenticated user",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 1,
                                      "userId": 3,
                                      "username": "SarSmi",
                                      "currency": "USD",
                                      "amount": 5000.0000,
                                      "version": 0,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Balance exists but belongs to another customer",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "You can only access your own balances",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances/5"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Balance with this ID does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "CurrencyBalance with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances/999"
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
                                      "message": "Method parameter 'id': Failed to convert value of type 'String' to required type 'Integer'",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/balances/abc"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Wrong role")
    })
    public ResponseEntity<CurrencyBalanceReadDto> getMyBalanceById(
            @Parameter(
                    description = "Balance ID",
                    required = true,
                    example = "1"
            )
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
