package org.example.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserUpdateDto;
import org.example.service.UserService;
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
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - User Management", description = "Admin endpoints for managing users")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get all users with pagination and filtering",
            description = """
                    Retrieves a paginated list of all users with optional filtering.
                    Supports filtering by role, username, and date ranges.
                    Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Page number (0-based)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of users per page",
                            example = "20"
                    ),
                    @Parameter(
                            name = "role",
                            description = "Filter by user role",
                            example = "CUSTOMER"
                    ),
                    @Parameter(
                            name = "username",
                            description = "Filter by username (partial match)",
                            example = "admin"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "username": "admin_first",
                                          "role": "ADMIN",
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
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public PageDto<UserReadDto> getAll(@RequestParam(required = false) Map<String, String> allParams) {
        log.debug("GET /admin/users - Params: {}", allParams);

        return userService.getAll(allParams);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by their unique ID. Requires ADMIN role.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User ID",
                            example = "1",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
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
                                      "path": "/admin/users/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<UserReadDto> getById(@PathVariable Integer id) {
        UserReadDto user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    @Operation(
            summary = "Create new user",
            description = "Creates a new user with the specified credentials and role. Requires ADMIN role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Create Admin",
                                            value = """
                                            {
                                              "username": "new_admin",
                                              "password": "securePassword123",
                                              "role": "ADMIN"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Create Customer",
                                            value = """
                                            {
                                              "username": "new_customer",
                                              "password": "securePassword123",
                                              "role": "CUSTOMER"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate username",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "errors": {
                                        "username": "must not be blank"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<UserReadDto> create(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserReadDto createdUser = userService.create(userCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdUser);
    }

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates an existing user's information. Requires ADMIN role.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User ID to update",
                            example = "5",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update details (all fields optional)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "username": "updated_username",
                                      "password": "newPassword123",
                                      "role": "ADMIN"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<UserReadDto> update(@PathVariable Integer id,
                                                      @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserReadDto updatedUser = userService.update(id, userUpdateDto);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete user",
            description = """
                    Deletes a user by ID. This will also cascade delete all associated currency balances.
                    Use with caution. Requires ADMIN role.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User ID to delete",
                            example = "10",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
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
                                      "message": "User with ID 999 not found"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("DELETE /admin/users/{}", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
