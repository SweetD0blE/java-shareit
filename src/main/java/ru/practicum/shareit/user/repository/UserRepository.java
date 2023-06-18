package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User findById(Long userId);

    User create(User user);

    User update(User user);

    void delete(Long userId);

    boolean isEmailExist(String email);

    boolean userNotExist(Long userId);
}
