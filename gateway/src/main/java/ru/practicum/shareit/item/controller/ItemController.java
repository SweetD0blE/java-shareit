package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                      @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping(value = "{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                              @PathVariable("id") @Positive long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSearchItem(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                @RequestParam(name = "text", defaultValue = "") String text,
                                                @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemClient.getSearchItem(userId, text, from, size);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                             @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemClient.addItem(userId, itemDto);
    }

    @PostMapping(value = "{id}/comment", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createCommentItem(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                    @PathVariable("id") @Positive long itemId,
                                                    @Validated @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @PatchMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                             @PathVariable("id") @Positive long itemId, @RequestBody ItemDto itemDto) {
        return itemClient.patchItem(userId, itemId, itemDto);
    }

}