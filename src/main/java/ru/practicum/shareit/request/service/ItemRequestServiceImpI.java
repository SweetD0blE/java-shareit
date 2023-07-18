package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ItemRequestServiceImpI implements ItemRequestService {

    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<ItemRequestDto> getAllRequestsWithOffers(Long userId) {
        userService.validateUserById(userId);
        Sort sort = Sort.by("created").descending();
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_Id(userId, sort);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return addItemsInResponseToRequest(requests);
    }

    @Override
    public ItemRequestDto getRequestWithOffersById(Long userId, Long requestId) {
        userService.validateUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Запрос с id = %d не был найден", requestId)));
        List<Item> items = itemRepository.findAllByRequest_IdOrderByRequestDesc(requestId);
        List<ItemDto> itemDto = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(itemDto);
        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getRequests(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNot(userId, page);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return addItemsInResponseToRequest(requests);
    }

    @Transactional
    @Override
    public ItemRequestDto saveRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userService.getById(userId);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository
                .save(ItemRequestMapper.toItemRequest(user, itemRequestDto)));
    }

    private List<ItemRequestDto> addItemsInResponseToRequest(List<ItemRequest> requests) {
        List<Long> requestsIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestsIds);
        List<ItemRequestDto> requestDto = requests.stream()
                .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
        List<ItemDto> itemDto = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : requestDto) {
            itemRequestDto.setItems(itemDto.stream()
                    .filter(i -> Objects.equals(i.getRequestId(), itemRequestDto.getId()))
                    .collect(Collectors.toList()));
        }
        return requestDto;
    }
}
