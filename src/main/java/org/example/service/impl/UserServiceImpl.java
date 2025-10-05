package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserUpdateDto;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserReadDto getById(int id) {
        return userRepository.findById(id)
                .map(userMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " not found"));
    }

    @Override
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
    public UserReadDto getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " not found"));
    }

    @Override
    public User getEntityById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " not found"));
    }

    @Override
    public void save(UserCreateDto userCreateDto) {
        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User with username " + userCreateDto.getUsername() + " already exists.");
        }

        String encodedPassword = passwordEncoder.encode(userCreateDto.getPassword());

        User user = User.builder()
                .username(userCreateDto.getUsername())
                .password(encodedPassword)
                .role(userCreateDto.getRole())
                .build();

        userRepository.save(user);
    }

    @Override
    public void update(int id, UserUpdateDto userUpdateDto) {
        User user = getEntityById(id);

        String encodedPassword = passwordEncoder.encode(userUpdateDto.getPassword());

        String newUsername = userUpdateDto.getUsername();
        String newPassword = encodedPassword;
        RoleName newRole = userUpdateDto.getRole();

        if (newUsername != null) {
            Optional<User> existingUser = userRepository.findByUsername(newUsername);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("User with Username '" + newUsername + "' already exists.");
            }
            user.setUsername(newUsername);
        }

        if (newPassword != null) {
            user.setPassword(newPassword);
        }

        if (newRole != null) {
            user.setRole(newRole);
        }

        userRepository.save(user);
    }

    @Override
    public void deleteById(int id) {
        getById(id);
        userRepository.deleteById(id);
    }
}
