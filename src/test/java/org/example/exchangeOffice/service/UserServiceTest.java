package org.example.exchangeOffice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.*;
import org.example.repository.UserRepository;
import org.example.repository.specification.SpecificationManager;
import org.example.service.impl.UserServiceImpl;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PageableBuilder pageableBuilder;

    @Mock
    private SpecificationManager<User> specificationManager;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserReadDto testUserReadDto;
    private UserCreateDto testUserCreateDto;
    private UserUpdateDto testUserUpdateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(RoleName.CUSTOMER);

        testUserReadDto = UserReadDto.builder()
                .id(1)
                .username("testuser")
                .role(RoleName.CUSTOMER)
                .build();

        testUserCreateDto = UserCreateDto.builder()
                .username("newuser")
                .password("Password123@")
                .role(RoleName.CUSTOMER)
                .build();

        testUserUpdateDto = UserUpdateDto.builder()
                .username("updateduser")
                .role(RoleName.ADMIN)
                .build();
    }

    @Test
    void getById_ExistingUser_ReturnsUserReadDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userMapper.toReadDto(testUser)).thenReturn(testUserReadDto);

        UserReadDto result = userService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1);
        verify(userMapper).toReadDto(testUser);
    }

    @Test
    void getById_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID: 999 not found");

        verify(userRepository).findById(999);
        verify(userMapper, never()).toReadDto(any());
    }

    @Test
    void getByUsername_ExistingUser_ReturnsUserReadDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toReadDto(testUser)).thenReturn(testUserReadDto);

        UserReadDto result = userService.getByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
        verify(userMapper).toReadDto(testUser);
    }

    @Test
    void getByUsername_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername("nonexistent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with username: nonexistent not found");

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getByIdWithBalances_ExistingUser_ReturnsUserWithBalances() {
        UserWithCurrencyBalanceReadDto userWithBalances = UserWithCurrencyBalanceReadDto.builder()
                .id(1)
                .username("testuser")
                .role(RoleName.CUSTOMER)
                .build();

        when(userRepository.findByIdWithBalances(1)).thenReturn(Optional.of(testUser));
        when(userMapper.toWithCurrencyBalanceReadDto(testUser)).thenReturn(userWithBalances);

        UserWithCurrencyBalanceReadDto result = userService.getByIdWithBalances(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(userRepository).findByIdWithBalances(1);
        verify(userMapper).toWithCurrencyBalanceReadDto(testUser);
    }

    @Test
    void getByIdWithBalances_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.findByIdWithBalances(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByIdWithBalances(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID 999 not found");

        verify(userRepository).findByIdWithBalances(999);
    }

    @Test
    void getAll_WithFilters_ReturnsPagedUsers() {
        Map<String, String> params = Map.of("role", "CUSTOMER", "page", "0", "size", "10");
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(pageableBuilder.buildFromFilters(params)).thenReturn(pageable);
        when(specificationManager.get(anyString(), any())).thenReturn(mock(Specification.class));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toReadDto(testUser)).thenReturn(testUserReadDto);

        PageDto<UserReadDto> result = userService.getAll(params);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void create_ValidUser_ReturnsCreatedUser() {
        User newUser = new User();
        newUser.setId(2);
        newUser.setUsername("newuser");
        newUser.setRole(RoleName.CUSTOMER);

        UserReadDto createdUserDto = UserReadDto.builder()
                .id(2)
                .username("newuser")
                .role(RoleName.CUSTOMER)
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(testUserCreateDto)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123@")).thenReturn("encodedPassword");
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userMapper.toReadDto(newUser)).thenReturn(createdUserDto);

        UserReadDto result = userService.create(testUserCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).findByUsername("newuser");
        verify(passwordEncoder).encode("Password123@");
        verify(userRepository).save(newUser);
    }

    @Test
    void create_DuplicateUsername_ThrowsIllegalArgumentException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.create(testUserCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with username newuser already exists");

        verify(userRepository).findByUsername("newuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_ValidData_ReturnsUpdatedUser() {
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setUsername("updateduser");
        updatedUser.setRole(RoleName.ADMIN);

        UserReadDto updatedUserDto = UserReadDto.builder()
                .id(1)
                .username("updateduser")
                .role(RoleName.ADMIN)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(updatedUser);
        when(userMapper.toReadDto(updatedUser)).thenReturn(updatedUserDto);

        UserReadDto result = userService.update(1, testUserUpdateDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getRole()).isEqualTo(RoleName.ADMIN);
        verify(userRepository).save(testUser);
    }

    @Test
    void update_DuplicateUsername_ThrowsIllegalArgumentException() {
        User existingUser = new User();
        existingUser.setId(2);
        existingUser.setUsername("updateduser");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.update(1, testUserUpdateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with Username 'updateduser' already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999, testUserUpdateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID: 999 not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_ValidPassword_UpdatesSuccessfully() {
        UserPasswordChangeDto passwordChangeDto = UserPasswordChangeDto.builder()
                .currentPassword("oldPassword")
                .newPassword("NewPassword123@")
                .confirmPassword("NewPassword123@")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123@", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123@")).thenReturn("newEncodedPassword");

        userService.updatePassword(1, passwordChangeDto);

        verify(passwordEncoder).encode("NewPassword123@");
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePassword_IncorrectCurrentPassword_ThrowsIllegalArgumentException() {
        UserPasswordChangeDto passwordChangeDto = UserPasswordChangeDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("NewPassword123@")
                .confirmPassword("NewPassword123@")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1, passwordChangeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_PasswordMismatch_ThrowsIllegalArgumentException() {
        UserPasswordChangeDto passwordChangeDto = UserPasswordChangeDto.builder()
                .currentPassword("oldPassword")
                .newPassword("NewPassword123@")
                .confirmPassword("DifferentPassword123@")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> userService.updatePassword(1, passwordChangeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("New password and confirmation do not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_SameAsCurrentPassword_ThrowsIllegalArgumentException() {
        UserPasswordChangeDto passwordChangeDto = UserPasswordChangeDto.builder()
                .currentPassword("oldPassword")
                .newPassword("oldPassword")
                .confirmPassword("oldPassword")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> userService.updatePassword(1, passwordChangeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("New password must be different from current password");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteById_ExistingUser_DeletesSuccessfully() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteById(1);

        verify(userRepository).existsById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteById_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID 999 not found");

        verify(userRepository).existsById(999);
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void getEntityById_ExistingUser_ReturnsUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        User result = userService.getEntityById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(userRepository).findById(1);
    }

    @Test
    void getEntityById_NonExistentUser_ThrowsEntityNotFoundException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getEntityById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID: 999 not found");

        verify(userRepository).findById(999);
    }
}
