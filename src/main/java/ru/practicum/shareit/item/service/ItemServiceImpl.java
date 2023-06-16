package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        return itemRepository.findItems()
                .stream()
                .filter(Item -> Item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.findById(itemId));
    }

    @Override
    public List<ItemDto> getSearchItem(Long userId, String text) {
        if (userRepository.userNotExist(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        String query = text.toLowerCase();
        return itemRepository.findItems()
                .stream()
                .filter(item -> item.getAvailable()
                        && item.getDescription().toLowerCase().contains(query)
                        || item.getName().contains(query))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemCreateDto) {
        User user = userRepository.findById(userId);
        return ItemMapper.toItemDto(itemRepository.create(user, ItemMapper.toItem(itemCreateDto, user)));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (userRepository.userNotExist(userId) || !itemRepository.findById(itemId).getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        if (itemRepository.itemNotExist(itemId)) {
            throw new ObjectNotFoundException(String.format("Товар с id = %d не найден", itemId));
        }
        Item item = itemRepository.findById(itemId);

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.update(item));
    }
}
