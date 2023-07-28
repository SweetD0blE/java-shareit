package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    public static final long ID = 1L;

    private UserService userService;
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
        user = new User(1L, "user", "mail@mail.ru");
    }

    @Test
    void getUserByIdTest() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(user));

        UserDto userDto = userService.getUserById(ID);

        assertNotNull(userDto);
        assertEquals(1, userDto.getId());

        verify(userRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void getAllUsersTest() {
        when(userRepository.findAll())
                .thenReturn(Collections.singletonList(user));

        List<UserDto> dto = userService.getAllUsers();

        assertNotNull(dto);
        assertEquals(1, dto.size());
        assertEquals(user.getId(), dto.get(0).getId());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createUserTest() {
        User savedUser = new User();
        savedUser.setName(user.getName());
        savedUser.setEmail(user.getEmail());
        UserDto savedDto = UserMapper.toUserDto(savedUser);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserDto userDto = userService.createUser(savedDto);

        assertNotNull(userDto);
        assertEquals(1, userDto.getId());
        assertEquals(savedUser.getName(), userDto.getName());
        assertEquals(savedUser.getEmail(), userDto.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserTest() {
        user.setName("update");
        UserDto inputDto = UserMapper.toUserDto(user);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));

        UserDto userDto = userService.updateUser(ID, inputDto);

        assertNotNull(userDto);
        assertEquals(userDto.getId(), 1);
        assertEquals(userDto.getName(), inputDto.getName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUserTest() {
        when(userRepository.existsById(ID))
                .thenReturn(true);

        userService.deleteUser(ID);

        verify(userRepository, times(1)).deleteById(ID);
    }

}
