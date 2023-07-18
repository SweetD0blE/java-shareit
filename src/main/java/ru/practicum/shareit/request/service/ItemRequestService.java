package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestDto> getAllRequestsWithOffers(Long userId);

    ItemRequestDto getRequestWithOffersById(Long userId, Long requestId);

    List<ItemRequestDto> getRequests(Long userId, Integer from, Integer size);

    ItemRequestDto saveRequest(Long userId, ItemRequestDto itemRequestDto);
}
