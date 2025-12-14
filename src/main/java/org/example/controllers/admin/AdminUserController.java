package org.example.controllers.admin;

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
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PageDto<UserReadDto> getAll(@RequestParam(required = false) Map<String, String> allParams) {
        log.debug("GET /admin/users - Params: {}", allParams);

        return userService.getAll(allParams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserReadDto> getById(@PathVariable Integer id) {
        UserReadDto user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
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
    public ResponseEntity<UserReadDto> update(@PathVariable Integer id,
                                                      @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserReadDto updatedUser = userService.update(id, userUpdateDto);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("DELETE /admin/users/{}", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
