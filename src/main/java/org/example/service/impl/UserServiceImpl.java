package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.*;
import org.example.repository.UserRepository;
import org.example.repository.specification.SpecificationManager;
import org.example.service.UserService;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageableBuilder pageableBuilder;
    private final SpecificationManager<User> specificationManager;

    private static final String SPLIT_TO_ARRAY = ",";

    @Override
    @Transactional(readOnly = true)
    public UserReadDto getById(Integer id) {
        return userRepository.findById(id)
                .map(userMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadDto getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithCurrencyBalanceReadDto getByIdWithBalances(Integer id) {
       return userRepository.findByIdWithBalances(id)
               .map(userMapper::toWithCurrencyBalanceReadDto)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<UserReadDto> getAll(Map<String, String> params) {
        Pageable pageRequest = pageableBuilder.buildFromFilters(params);
        Specification<User> specification = null;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<User> sp = specificationManager.get(entry.getKey(), entry.getValue().split(SPLIT_TO_ARRAY));
            specification = specification == null ? Specification.where(sp) : specification.and(sp);
        }

        Page<User> userPage = userRepository.findAll(specification, pageRequest);

        List<UserReadDto> userReadDtos = userPage.getContent().stream()
                .map(userMapper::toReadDto)
                .collect(Collectors.toList());

        return PageDto.<UserReadDto>builder()
                .content(userReadDtos)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public UserReadDto create(UserCreateDto userCreateDto) {
        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User with username " + userCreateDto.getUsername() + " already exists.");
        }

        User user = userMapper.toEntity(userCreateDto);

        String encodedPassword = passwordEncoder.encode(userCreateDto.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);
        return userMapper.toReadDto(savedUser);
    }

    @Override
    @Transactional
    public UserReadDto update(Integer id, UserUpdateDto userUpdateDto) {
        User user = getEntityById(id);

        String newUsername = userUpdateDto.getUsername();
        RoleName newRole = userUpdateDto.getRole();

        if (newUsername != null) {
            Optional<User> existingUser = userRepository.findByUsername(newUsername);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("User with Username '" + newUsername + "' already exists.");
            }
            user.setUsername(newUsername);
        }

        if (newRole != null) {
            user.setRole(newRole);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toReadDto(updatedUser);
    }

    @Override
    @Transactional
    public void updatePassword(Integer id, UserPasswordChangeDto passwordChangeDto) {
        User user = getEntityById(id);

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        String encodedPassword = passwordEncoder.encode(passwordChangeDto.getNewPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " not found"));
    }
}
