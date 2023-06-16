package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getUserItems(Long userId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getSearchItem(Long userId, String text);

    ItemDto createItem(Long userId, ItemDto itemCreateDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);
}
