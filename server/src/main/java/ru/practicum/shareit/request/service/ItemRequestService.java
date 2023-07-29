package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestDto> getAllRequestsWithOffers(Long userId);

    List<ItemRequestDto> getRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestWithOffersById(Long userId, Long requestId);

    ItemRequestDto saveRequest(Long userId, ItemRequestDto itemRequestDto);
}
