package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    @Override
    public List<Item> findItems() {
        log.info("Список товаров");
        return new ArrayList<>(items.values());
    }

    @Override
    public Item findById(Long itemId) {
        if(itemNotExist(itemId)) {
            throw new ObjectNotFoundException(String.format("Товар с id = %d не найден", itemId));
        }
        log.info("Найден товар с id = {}", itemId);
        return items.get(itemId);
    }

    @Override
    public Item create(User owner, Item item) {
        item.setId(id++);
        item.setOwner(owner);
        items.put(item.getId(), item);
        log.info("Товар с id = {} был создан", item.getId());
        return item;
    }

    @Override
    public Item update(Item item) {
        log.info("Товар с id = {} был обновлен", item.getId());
        return items.get(item.getId());
    }

    @Override
    public boolean itemNotExist(Long itemId) {
        return !items.containsKey(itemId);
    }
}
