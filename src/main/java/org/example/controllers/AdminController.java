package org.example.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserUpdateDto;
import org.example.service.UserService;
import org.example.utils.PageableBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final UserService userService;
    private final PageableBuilder pageableBuilder;

    @GetMapping
    public PageDto<UserReadDto> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        Pageable pageable = pageableBuilder.build(page, size, sortBy, sortOrder);
        return userService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserReadDto> getById(@PathVariable int id) {
        UserReadDto user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity<UserReadDto> create(@Valid @RequestBody UserCreateDto userCreateDto) {
        userService.save(userCreateDto);
        UserReadDto createdUser = userService.getByUsername(userCreateDto.getUsername());

        URI location = URI.create("/admin/users/" + createdUser.getId());
        return ResponseEntity
                .created(location)
                .body(createdUser);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserReadDto> update(@PathVariable int id,
                                                      @Valid @RequestBody UserUpdateDto userUpdateDto) {
        userService.update(id, userUpdateDto);
        UserReadDto updatedUser = userService.getById(id);

        URI location = URI.create("/admin/users/" + updatedUser.getId());
        return ResponseEntity
                .created(location)
                .body(updatedUser);
    }

    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable int id) {
        userService.deleteById(id);
    }
}
