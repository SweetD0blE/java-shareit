package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository {
    List<Item> findItems();

    Item findById(Long itemId);

    Item create(User owner, Item item);

    Item update(Item item);

    boolean itemNotExist(Long itemId);
}
