package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemOwnerDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                  @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.getAllItemsByUserId(userId, from, size);
    }

    @GetMapping("{id}")
    public ItemOwnerDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable("id") Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchItem(@RequestParam(name = "text", defaultValue = "") String text,
                                       @RequestParam(name = "from", defaultValue = "0") Integer from,
                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.getSearchItem(text, from, size);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("id") Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @PostMapping("{id}/comment")
    public CommentDto createCommentItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PathVariable("id") Long itemId,
                                        @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }

}
