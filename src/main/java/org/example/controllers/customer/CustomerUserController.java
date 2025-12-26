package org.example.controllers.customer;

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
    public ResponseEntity<UserWithCurrencyBalanceReadDto> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.debug("GET /customer/profile - User: {}", userId);
        UserWithCurrencyBalanceReadDto profile = userService.getByIdWithBalances(userId);

        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UserPasswordChangeDto passwordChangeDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        log.info("PATCH /customer/profile/password - User: {}", userId);
        userService.updatePassword(userId, passwordChangeDto);

        return ResponseEntity.noContent().build();
    }
}
