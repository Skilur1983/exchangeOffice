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
@Tag(name = "Admin - Currency Balance", description = "Admin endpoints for managing currency balances")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCurrencyBalanceController {

    private final CurrencyBalanceService currencyBalanceService;

    @GetMapping
    @Operation(
            summary = "Get all currency balances with pagination and filtering",
            description = """
                    Retrieves a paginated list of all currency balances across all users.
                    
                    **Filtering Options:**
                    - userId: Filter balances by specific user ID
                    - currency: Filter by currency type (USD, EUR, UAH)
                    - amountBetween: Filter by amount range (min,max)
                    - createdAfter: Balances created after date (YYYY-MM-DD)
                    - createdBefore: Balances created before date (YYYY-MM-DD)
                    
                    **Pagination:**
                    - page: Page number (0-based)
                    - size: Number of records per page
                    - sortBy: Field to sort by (e.g., amount, currency, createdAt)
                    - sortOrder: asc or desc
                    
                    **Use Cases:**
                    - Monitor all customer balances
                    - Find users with low/high balances
                    - Audit balance distribution
                    - Generate financial reports
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "Filter by specific user ID",
                            example = "3"
                    ),
                    @Parameter(
                            name = "currency",
                            description = "Filter by currency (USD, EUR, UAH)",
                            example = "USD"
                    ),
                    @Parameter(
                            name = "amountBetween",
                            description = "Filter by amount range (min,max)",
                            example = "1000,5000"
                    ),
                    @Parameter(
                            name = "createdAfter",
                            description = "Balances created after this date (YYYY-MM-DD)",
                            example = "2024-12-01"
                    ),
                    @Parameter(
                            name = "createdBefore",
                            description = "Balances created before this date (YYYY-MM-DD)",
                            example = "2024-12-23"
                    ),
                    @Parameter(
                            name = "page",
                            description = "Page number (0-based)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of balances per page",
                            example = "20"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balances retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "content": [
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
                                        }
                                      ],
                                      "pageNumber": 0,
                                      "pageSize": 20,
                                      "totalElements": 24,
                                      "totalPages": 2
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public PageDto<CurrencyBalanceReadDto> getAll(@RequestParam(required = false) Map<String, String> filters) {
        log.debug("GET /admin/balances - Filters: {}", filters);

        return currencyBalanceService.getAll(filters);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get currency balance by ID",
            description = """
                    Retrieves a specific currency balance by its unique ID.
                    
                    Returns complete balance information including:
                    - User details (ID and username)
                    - Currency type
                    - Current amount
                    - Version (for optimistic locking)
                    - Timestamps
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Currency balance ID",
                            example = "1",
                            required = true
                    )
            }
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
                    description = "Balance not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "CurrencyBalance with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<CurrencyBalanceReadDto> getById(@PathVariable Integer id) {
        log.debug("GET /admin/balances/{}", id);

        CurrencyBalanceReadDto balance = currencyBalanceService.getById(id);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get all balances for a specific user",
            description = """
                    Retrieves all currency balances for a specific user.
                    
                    Returns a list of all currencies the user holds:
                    - USD balance (if exists)
                    - EUR balance (if exists)
                    - UAH balance (if exists)
                    
                    **Note:** Users can have 0-3 balances depending on which currencies they've used.
                    An empty list means the user has no balances yet.
                    
                    **Use Cases:**
                    - View customer's complete portfolio
                    - Verify user has sufficient funds before deal
                    - Generate customer account statements
                    - Customer support inquiries
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "User ID to get balances for",
                            example = "3",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User balances retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "User with Multiple Balances",
                                            summary = "User has USD, EUR, and UAH",
                                            value = """
                                            [
                                              {
                                                "id": 3,
                                                "userId": 4,
                                                "username": "TomBro",
                                                "currency": "USD",
                                                "amount": 2500.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              },
                                              {
                                                "id": 4,
                                                "userId": 4,
                                                "username": "TomBro",
                                                "currency": "EUR",
                                                "amount": 4000.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              },
                                              {
                                                "id": 5,
                                                "userId": 4,
                                                "username": "TomBro",
                                                "currency": "UAH",
                                                "amount": 100000.0000,
                                                "version": 0,
                                                "createdAt": "2024-12-23T10:00:00",
                                                "updatedAt": "2024-12-23T10:00:00"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "User with Single Balance",
                                            summary = "User has only EUR",
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
                                            name = "User with No Balances",
                                            summary = "New user without any balances",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/user/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<List<CurrencyBalanceReadDto>> getByUserId(@PathVariable Integer userId) {
        log.debug("GET /admin/balances/user/{}", userId);

        List<CurrencyBalanceReadDto> balances = currencyBalanceService.getAllByUserId(userId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/user/{userId}/currency/{currency}")
    @Operation(
            summary = "Get specific user's balance for specific currency",
            description = """
                    Retrieves a user's balance for a specific currency.
                    
                    **Use Cases:**
                    - Check if user has enough USD for a deal
                    - Verify customer's EUR balance before withdrawal
                    - Quick balance lookup for specific currency
                    - Validate funds availability
                    
                    **Note:** Returns 404 if the user doesn't have a balance in that currency.
                    Users only have balances for currencies they've deposited or received.
                    
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "User ID",
                            example = "3",
                            required = true
                    ),
                    @Parameter(
                            name = "currency",
                            description = "Currency code (USD, EUR, or UAH)",
                            example = "USD",
                            required = true
                    )
            }
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
                    description = "Balance not found for this user and currency",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Balance not found for user 3 and currency EUR",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/user/3/currency/EUR"
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
                                      "path": "/admin/balances/user/3/currency/GBP"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<CurrencyBalanceReadDto> getByUserAndCurrency(
            @PathVariable Integer userId,
            @PathVariable Currency currency) {
        log.debug("GET /admin/balances/user/{}/currency/{}", userId, currency);

        CurrencyBalanceReadDto balance = currencyBalanceService.getByUserAndCurrency(userId, currency);
        return ResponseEntity.ok(balance);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new currency balance",
            description = """
                    Creates a new currency balance for a user.
                    
                    **Important Constraints:**
                    - Each user can have only ONE balance per currency
                    - Attempting to create duplicate will fail with 409 Conflict
                    - Initial amount can be 0 or positive (never negative)
                    
                    **When to Use:**
                    - Customer opens new currency account
                    - Customer makes first deposit in a currency
                    - Admin initializes customer portfolio
                    - System creates balance for new deal currency
                    
                    **Alternative Approach:**
                    Instead of creating manually, consider using POST /deposit which creates 
                    the balance automatically if it doesn't exist.
                    
                    Requires ADMIN role.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Balance creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyBalanceCreateDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Create with Initial Amount",
                                            summary = "Create USD balance with 1000 initial deposit",
                                            value = """
                                            {
                                              "userId": 12,
                                              "currency": "USD",
                                              "amount": 1000.00
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Create Empty Balance",
                                            summary = "Create EUR balance starting at zero",
                                            value = """
                                            {
                                              "userId": 12,
                                              "currency": "EUR",
                                              "amount": 0
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Create UAH Balance",
                                            summary = "Create UAH balance with large amount",
                                            value = """
                                            {
                                              "userId": 12,
                                              "currency": "UAH",
                                              "amount": 50000.00
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
                    description = "Balance created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 25,
                                      "userId": 12,
                                      "username": "NewCustomer",
                                      "currency": "USD",
                                      "amount": 1000.0000,
                                      "version": 0,
                                      "createdAt": "2024-12-23T10:30:00",
                                      "updatedAt": "2024-12-23T10:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing Fields",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "userId": "must not be null",
                                                "currency": "must not be null"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Negative Amount",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Amount cannot be negative",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Balance already exists for this user and currency",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Balance already exists for user 3 and currency USD",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
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
    @Operation(
            summary = "Deposit funds into user's balance",
            description = """
                    Adds funds to a user's currency balance. Creates the balance if it doesn't exist.
                    
                    **How it Works:**
                    1. Checks if user has balance for this currency
                    2. If exists: Adds amount to current balance
                    3. If not exists: Creates new balance with deposit amount
                    4. Updates version (optimistic locking)
                    5. Records transaction with optional description
                    
                    **Use Cases:**
                    - Customer deposits cash at office
                    - Wire transfer received
                    - Initial account funding
                    - Bonus/promotion credits
                    - Refund processing
                    
                    **Best Practices:**
                    - Always include description for audit trail
                    - Description should explain source of funds
                    - For large deposits, consider additional verification
                    
                    **Transaction Safety:**
                    Uses optimistic locking to prevent concurrent modification issues.
                    
                    Requires ADMIN role.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deposit details including optional description",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BalanceDepositDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Cash Deposit",
                                            summary = "Customer deposits cash at office",
                                            value = """
                                            {
                                              "userId": 3,
                                              "currency": "USD",
                                              "amount": 500.00,
                                              "description": "Cash deposit at office - Receipt #12345"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Wire Transfer",
                                            summary = "Received bank transfer",
                                            value = """
                                            {
                                              "userId": 4,
                                              "currency": "EUR",
                                              "amount": 2000.00,
                                              "description": "Wire transfer from Bank of Example - Ref: WT789456"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Initial Funding",
                                            summary = "New account initial deposit",
                                            value = """
                                            {
                                              "userId": 12,
                                              "currency": "UAH",
                                              "amount": 10000.00,
                                              "description": "Initial account funding"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Without Description",
                                            summary = "Minimum required fields (not recommended)",
                                            value = """
                                            {
                                              "userId": 3,
                                              "currency": "USD",
                                              "amount": 100.00
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
                    description = "Deposit successful - Balance updated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 1,
                                      "userId": 3,
                                      "username": "SarSmi",
                                      "currency": "USD",
                                      "amount": 5500.0000,
                                      "version": 1,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:35:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or negative amount",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Negative Amount",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Deposit amount must be positive",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/deposit"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Zero Amount",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Deposit amount must be greater than zero",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/deposit"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User with ID: 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/deposit"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Concurrent modification detected (optimistic lock failure)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Balance was modified by another transaction. Please retry.",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/deposit"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
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
    @Operation(
            summary = "Withdraw funds from user's balance",
            description = """
                    Withdraws funds from a user's currency balance.
                    
                    **How it Works:**
                    1. Verifies user has balance for this currency
                    2. Checks sufficient funds available
                    3. Deducts amount from current balance
                    4. Updates version (optimistic locking)
                    5. Records transaction with optional description
                    
                    **Important Validations:**
                    - User must have existing balance in that currency
                    - Balance must be sufficient (current >= withdrawal amount)
                    - Amount must be positive
                    
                    **Use Cases:**
                    - Customer withdraws cash from office
                    - Wire transfer sent
                    - Payment processing
                    - Fee deductions
                    
                    **Best Practices:**
                    - Always include description for audit trail
                    - Description should explain purpose of withdrawal
                    - For large withdrawals, verify customer identity
                    - Consider daily withdrawal limits
                    
                    **Error Handling:**
                    Returns 400 if insufficient funds, not 409.
                    Uses optimistic locking for concurrent access safety.
                    
                    Requires ADMIN role.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Withdrawal details including optional description",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BalanceWithdrawalDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Cash Withdrawal",
                                            summary = "Customer withdraws cash from office",
                                            value = """
                                            {
                                              "userId": 3,
                                              "currency": "USD",
                                              "amount": 200.00,
                                              "description": "Cash withdrawal - Receipt #12346"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Wire Transfer Out",
                                            summary = "Customer requests bank transfer",
                                            value = """
                                            {
                                              "userId": 4,
                                              "currency": "EUR",
                                              "amount": 1500.00,
                                              "description": "Wire transfer to account DE89370400440532013000"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Service Fee",
                                            summary = "Deduct service fee",
                                            value = """
                                            {
                                              "userId": 3,
                                              "currency": "UAH",
                                              "amount": 50.00,
                                              "description": "Monthly maintenance fee - December 2024"
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
                    description = "Withdrawal successful - Balance updated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "id": 1,
                                      "userId": 3,
                                      "username": "SarSmi",
                                      "currency": "USD",
                                      "amount": 4800.0000,
                                      "version": 2,
                                      "createdAt": "2024-12-23T10:00:00",
                                      "updatedAt": "2024-12-23T10:40:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error, negative amount, or insufficient funds",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Insufficient Funds",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Insufficient balance. Available: 1000.00, Required: 2000.00",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/withdraw"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Negative Amount",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Withdrawal amount must be positive",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/withdraw"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or balance not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "User Not Found",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "User with ID: 999 not found",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/withdraw"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Balance Not Found",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Balance not found for user 3 and currency GBP",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/withdraw"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Concurrent modification detected (optimistic lock failure)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Balance was modified by another transaction. Please retry.",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/withdraw"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
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
    @Operation(
            summary = "Delete currency balance",
            description = """
                    Deletes a currency balance by ID.
                    
                    **⚠️ DANGER - USE WITH EXTREME CAUTION ⚠️**
                    
                    **Before Deleting, Consider:**
                    - Is there money in this balance? (should be withdrawn first)
                    - Are there pending deals using this balance?
                    - Is this part of an audit trail?
                    - Should you set to zero instead of deleting?
                    
                    **When Deletion is Appropriate:**
                    - Zero-balance accounts being closed
                    - Test data cleanup
                    - Duplicate records created in error
                    - Account closure after full withdrawal
                    
                    **Recommended Workflow:**
                    1. Verify balance is zero (or withdraw all funds first)
                    2. Check for no pending/active deals
                    3. Confirm with customer
                    4. Delete balance
                    5. Log action with reason
                    
                    **Better Alternative:**
                    Instead of deleting, consider:
                    - Withdrawing all funds (balance becomes zero)
                    - Keeping zero balances for audit history
                    - Implementing "archived" status instead
                    
                    **What Gets Deleted:**
                    - The balance record itself
                    - User can still have other currency balances
                    - Historical deal references preserved
                    
                    Use with extreme caution. Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Currency balance ID to delete",
                            example = "25",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Balance deleted successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Balance not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "CurrencyBalance with ID 999 not found",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/admin/balances/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Cannot delete balance with funds or active references",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Non-Zero Balance",
                                            value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Cannot delete balance with non-zero amount: 1000.00 USD",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/1"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Active Deals",
                                            value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Cannot delete balance: 3 pending deals reference this balance",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/admin/balances/1"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.warn("DELETE /admin/balances/{} - Deleting currency balance", id);
        currencyBalanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
