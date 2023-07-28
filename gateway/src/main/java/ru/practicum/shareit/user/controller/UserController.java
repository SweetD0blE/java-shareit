package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.GroupValidation.Create;
import ru.practicum.shareit.validation.GroupValidation.Update;

import javax.validation.constraints.Positive;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAll() {
        return userClient.getAllUsers();
    }

    @GetMapping(value = "{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getById(@PathVariable("id") @Positive long userId) {
        return userClient.getById(userId);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> add(@Validated(Create.class) @RequestBody UserDto userDto) {
        return userClient.addUser(userDto);
    }

    @PatchMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> patch(@PathVariable("id") @Positive long userId,
                                        @Validated(Update.class) @RequestBody UserDto userDto) {
        return userClient.patchUser(userId, userDto);
    }

    @DeleteMapping(value = "{id}", produces = APPLICATION_JSON_VALUE)
    public void deleteUser(@PathVariable("id") @Positive long userId) {
        userClient.deleteUser(userId);
    }
}
