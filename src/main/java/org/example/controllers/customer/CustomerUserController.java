package org.example.controllers.customer;

import io.swagger.v3.oas.annotations.Operation;
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
import org.example.model.dto.user.UserPasswordChangeDto;
import org.example.model.dto.user.UserWithCurrencyBalanceReadDto;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/customer/profile")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer - Profile", description = "Customer endpoints for profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerUserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get my profile with balances",
            description = """
                    Retrieves the authenticated customer's complete profile including all currency balances.
                    
                    **Returns:**
                    - User account information (ID, username, role)
                    - Email address
                    - Account status (enabled, locked)
                    - All currency balances (0-3 currencies)
                    - Account creation date
                    
                    **Profile Information Includes:**
                    - **Account Details**: Username, email, role
                    - **Security Status**: Account enabled/disabled, locked/unlocked
                    - **Financial Summary**: All currency balances in one view
                    - **Metadata**: Created/updated timestamps
                    
                    **Use Cases:**
                    - View complete account overview
                    - Check all currency balances at once
                    - Display profile in mobile/web app
                    - Verify account information
                    - Financial portfolio summary
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Automatically scoped to authenticated user
                    - Password NOT included in response
                    - Cannot view other customers' profiles
                    
                    **Note:** This endpoint combines user info + balances for convenience.
                    To view balances only, use GET /customer/balances instead.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Customer with Multiple Balances",
                                            summary = "Complete profile with USD, EUR, and UAH balances",
                                            value = """
                                            {
                                              "id": 3,
                                              "username": "SarSmi",
                                              "email": "sarah.smith@example.com",
                                              "role": "CUSTOMER",
                                              "enabled": true,
                                              "accountNonLocked": true,
                                              "createdAt": "2024-12-20T10:00:00",
                                              "updatedAt": "2024-12-23T15:30:00",
                                              "currencyBalances": [
                                                {
                                                  "id": 1,
                                                  "currency": "USD",
                                                  "amount": 5000.0000,
                                                  "version": 2
                                                },
                                                {
                                                  "id": 2,
                                                  "currency": "EUR",
                                                  "amount": 3000.0000,
                                                  "version": 1
                                                },
                                                {
                                                  "id": 10,
                                                  "currency": "UAH",
                                                  "amount": 120000.0000,
                                                  "version": 0
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Customer with Single Balance",
                                            summary = "Profile with only EUR balance",
                                            value = """
                                            {
                                              "id": 6,
                                              "username": "MarJon",
                                              "email": "mark.jones@example.com",
                                              "role": "CUSTOMER",
                                              "enabled": true,
                                              "accountNonLocked": true,
                                              "createdAt": "2024-12-20T10:00:00",
                                              "updatedAt": "2024-12-20T10:00:00",
                                              "currencyBalances": [
                                                {
                                                  "id": 9,
                                                  "currency": "EUR",
                                                  "amount": 10000.0000,
                                                  "version": 0
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "New Customer without Balances",
                                            summary = "Profile with no currency balances yet",
                                            value = """
                                            {
                                              "id": 12,
                                              "username": "NewCustomer",
                                              "email": "new.customer@example.com",
                                              "role": "CUSTOMER",
                                              "enabled": true,
                                              "accountNonLocked": true,
                                              "createdAt": "2024-12-23T10:00:00",
                                              "updatedAt": "2024-12-23T10:00:00",
                                              "currencyBalances": []
                                            }
                                            """
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
                                      "path": "/customer/profile"
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
                                      "path": "/customer/profile"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<UserWithCurrencyBalanceReadDto> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/profile - User: {}", userId);
        UserWithCurrencyBalanceReadDto profile = userService.getByIdWithBalances(userId);

        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/password")
    @Operation(
            summary = "Change my password",
            description = """
                    Allows the authenticated customer to change their password.
                    
                    **Security Requirements:**
                    1. **Current Password Required**: Must provide correct current password
                    2. **Password Validation**: New password must meet security requirements
                    3. **Confirmation Required**: New password must match confirmation
                    4. **Cannot Reuse**: New password must be different from current
                    
                    **Password Requirements:**
                    - Minimum 8 characters
                    - Maximum 100 characters
                    - At least one uppercase letter (A-Z)
                    - At least one lowercase letter (a-z)
                    - At least one digit (0-9)
                    - At least one special character (@$!%*?&)
                    - No whitespace allowed
                    
                    **Process:**
                    1. Validates current password is correct
                    2. Validates new password meets requirements
                    3. Validates new password matches confirmation
                    4. Hashes new password with BCrypt
                    5. Updates password in database
                    6. Returns 204 No Content on success
                    
                    **Use Cases:**
                    - Regular password rotation for security
                    - Suspected password compromise
                    - Compliance with password policies
                    - User-initiated security improvement
                    
                    **Security:**
                    - Requires CUSTOMER role
                    - Must be authenticated with valid token
                    - Current password verification prevents unauthorized changes
                    - Cannot change other users' passwords
                    
                    **Note:** After password change, current JWT token remains valid.
                    User does NOT need to log in again immediately, but should on next session.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserPasswordChangeDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Valid Password Change",
                                            summary = "Correct format with all requirements met",
                                            value = """
                                            {
                                              "currentPassword": "OldPassword123!",
                                              "newPassword": "NewSecurePass456@",
                                              "confirmPassword": "NewSecurePass456@"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Strong Password Example",
                                            summary = "Complex password following best practices",
                                            value = """
                                            {
                                              "currentPassword": "password",
                                              "newPassword": "MyS3cur3P@ssw0rd!2024",
                                              "confirmPassword": "MyS3cur3P@ssw0rd!2024"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully - No content returned"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or incorrect current password",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Incorrect Current Password",
                                            summary = "Current password does not match",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Current password is incorrect",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Password Mismatch",
                                            summary = "New password and confirmation don't match",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "New password and confirmation password do not match",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Weak Password - Too Short",
                                            summary = "Password doesn't meet minimum length",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "newPassword": "Password must be at least 8 characters long"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Weak Password - Missing Requirements",
                                            summary = "Password missing required character types",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "newPassword": "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Missing Required Fields",
                                            summary = "One or more fields not provided",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "currentPassword": "must not be blank",
                                                "newPassword": "must not be blank",
                                                "confirmPassword": "must not be blank"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Same as Current Password",
                                            summary = "New password identical to current",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "New password must be different from current password",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/customer/profile/password"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated - Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Authentication token has expired",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/profile/password"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Wrong role",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access denied",
                                      "timestamp": "2024-12-23T10:30:00",
                                      "path": "/customer/profile/password"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UserPasswordChangeDto passwordChangeDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.info("PATCH /customer/profile/password - User: {}", userId);
        userService.updatePassword(userId, passwordChangeDto);

        return ResponseEntity.noContent().build();
    }
}
