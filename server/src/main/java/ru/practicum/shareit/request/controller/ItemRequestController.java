package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestDto> getAllRequestsWithOffers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestsWithOffers(userId);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getRequestWithOffersById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable("requestId") Long requestId) {
        return itemRequestService.getRequestWithOffersById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(name = "from", required = false, defaultValue = "0")
                                            Integer from,
                                            @RequestParam(name = "size", required = false, defaultValue = "500")
                                            Integer size) {
        return itemRequestService.getRequests(userId, from, size);
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.saveRequest(userId, itemRequestDto);
    }
}
