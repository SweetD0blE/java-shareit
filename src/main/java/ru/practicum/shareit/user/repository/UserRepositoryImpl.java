package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @Override
    public List<User> findAll() {
        log.info("Список пользователей:");
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Long userId) {
        if (userNotExist(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        log.info("Получен пользователь с id = {} ", userId);
        return users.get(userId);
    }

    @Override
    public User create(User user) {
        user.setId(id++);
        users.put(user.getId(), user);
        log.info("Пользователь с id = {} был создан", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Пользователь с id = {} обновлен", user.getId());
        return users.get(user.getId());
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
        log.info("Пользователь с id = {} удален", userId);
    }

    @Override
    public boolean isEmailExist(String email) {
        return users.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(emailUser -> emailUser.equals(email));
    }

    @Override
    public boolean userNotExist(Long userId) {
        return !users.containsKey(userId);
    }
}
