package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    private User user;

    @BeforeEach
    private void beforeEach() {
        user = new User(1L, "user", "mail@mail.ru");
    }

    @Test
    public void toUserDtoTest() {
        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    public void toUserTest() {
        UserDto dto = new UserDto(1L, "user", "mail.@mail.ru");
        User fromDtoUser = UserMapper.toUser(dto);

        assertEquals(dto.getId(), fromDtoUser.getId());
        assertEquals(dto.getName(), fromDtoUser.getName());
        assertEquals(dto.getEmail(), fromDtoUser.getEmail());
    }

}
