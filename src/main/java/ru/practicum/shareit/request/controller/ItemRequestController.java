package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestDto> getAllRequestsWithOffers(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.getAllRequestsWithOffers(userId);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getRequestWithOffersById(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                   @PathVariable("requestId") @Positive Long requestId) {
        return itemRequestService.getRequestWithOffersById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                            @RequestParam(name = "from", required = false, defaultValue = "0")
                                            @PositiveOrZero Integer from,
                                            @RequestParam(name = "size", required = false, defaultValue = "500")
                                            @Positive Integer size) {
        return itemRequestService.getRequests(userId, from, size);
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                        @Validated(Create.class) @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.saveRequest(userId, itemRequestDto);
    }
}
