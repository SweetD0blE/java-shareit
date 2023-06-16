package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getUserItems(@Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.getUserItems(userId);
    }

    @GetMapping("{id}")
    public ItemDto getItemById(@PathVariable("id") @Positive Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchItem(@Valid@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                       @RequestParam String text) {
        return itemService.getSearchItem(userId, text);
    }

    @PostMapping
    public ItemDto createItem(@Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("{id}")
    public ItemDto updateItem(@Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @PathVariable("id") @Positive Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

}
