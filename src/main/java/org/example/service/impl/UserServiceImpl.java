package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.*;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserReadDto getById(int id) {
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
    public PageDto<UserReadDto> getAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserReadDto> userReadDtos = userPage.stream()
                .map(userMapper::toReadDto)
                .toList();

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
    public UserReadDto update(int id, UserUpdateDto userUpdateDto) {
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
    public void updatePassword(int id, UserPasswordChangeDto passwordChangeDto) {
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
    public void deleteById(int id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " not found"));
    }
}
