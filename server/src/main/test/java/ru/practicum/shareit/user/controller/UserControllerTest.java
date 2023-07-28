package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    public void getAllUsersTest() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void createUserTest() throws Exception {
        long userId = 1L;
        UserDto userDto = createTestUserDto(userId);

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(userDto)));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    public void getUserByIdTest() throws Exception {
        long userId = 1L;
        UserDto userDto = createTestUserDto(userId);

        when(userService.getUserById(userId))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(userDto)));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    public void updateUserTest() throws Exception {
        long userId = 1L;
        UserDto userDto = createTestUserDto(userId);
        userDto.setName("update");

        when(userService.updateUser(any(Long.class), any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(userDto)));

        verify(userService, times(1)).updateUser(any(Long.class), any(UserDto.class));
    }

    @Test
    public void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(any(Long.class));
    }

    private UserDto createTestUserDto(Long id) {
        String name = "user";
        String email = "mail@mail.ru";

        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private String toJson(UserDto dto) {
        return String.format("{\"id\":%d,\"name\": %s,\"email\": %s}", dto.getId(), dto.getName(), dto.getEmail());
    }
}
