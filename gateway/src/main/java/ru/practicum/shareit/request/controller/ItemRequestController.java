package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestClient.getRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size) {
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable long requestId) {
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @Validated(Create.class) @RequestBody ItemRequestDto itemRequestNewDto) {
        return itemRequestClient.addRequest(userId, itemRequestNewDto);
    }

}