package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    ItemRequestServiceImpl requestService;

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    private User requestor;
    private User owner;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("mail@mail.ru");
        owner.setId(1L);

        requestor = new User();
        requestor.setName("name2");
        requestor.setEmail("mail2@mail.ru");
        requestor.setId(2L);

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        request.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
    }

    @Test
    void getAllRequestsWithOffers_ReturnListItemsRequestTest() {
        long userId = requestor.getId();
        when(itemRequestRepository.findAllByRequestor_Id(userId, SORT)).thenReturn(List.of(request));

        List<ItemRequestDto> requests = requestService.getAllRequestsWithOffers(userId);

        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(itemRequestRepository, times(1)).findAllByRequestor_Id(userId, SORT);
    }

    @Test
    void getRequestWithOffersById_ReturnItemRequestTest() {
        long userId = requestor.getId();
        long requestId = request.getId();
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequest_IdOrderByRequestDesc(requestId)).thenReturn(List.of(item));

        ItemRequestDto requestDto = requestService.getRequestWithOffersById(userId, requestId);

        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());

        InOrder inOrder = inOrder(itemRequestRepository, itemRepository);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(itemRepository).findAllByRequest_IdOrderByRequestDesc(requestId);
    }

    @Test
    void getRequests_ReturnListItemRequestTest() {
        long userId = owner.getId();
        long requestId = request.getId();
        PageRequest page = PageRequest.of(0, 1, SORT);
        when(itemRepository.findByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));
        when(itemRequestRepository.findAllByRequestor_IdNot(userId, page)).thenReturn(List.of(request));
        List<ItemRequestDto> requestDtos = requestService.getRequests(userId, 0, 1);
        assertNotNull(requestDtos);
        assertEquals(1, requestDtos.size());
    }

    @Test
    void getRequests_ReturnEmptyListTest() {
        long userId = requestor.getId();
        PageRequest page = PageRequest.of(0, 1, SORT);
        when(itemRequestRepository.findAllByRequestor_IdNot(userId, page)).thenReturn(Collections.emptyList());
        List<ItemRequestDto> requestDtos = requestService.getRequests(userId, 0, 1);
        assertNotNull(requestDtos);
        assertEquals(0, requestDtos.size());
    }

    @Test
    void saveRequest_ReturnItemRequestDtoTest() {
        when(itemRequestRepository.save(any())).thenReturn(request);
        when(userService.getById(requestor.getId())).thenReturn(requestor);

        ItemRequestDto requestDto = requestService.saveRequest(requestor.getId(),
                ItemRequestDto.builder().description("description").build());

        assertNotNull(requestDto);
        assertEquals(request.getId(), requestDto.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void saveRequestWhenUserNotFound_ReturnObjectNotFoundExceptionTest() {
        long userIdNotFound = 999L;
        String error = String.format("Пользователь с id = %d не был найден", userIdNotFound);
        when(userService.getById(userIdNotFound)).thenThrow(new ObjectNotFoundException(error));
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> requestService.saveRequest(userIdNotFound,
                        ItemRequestDto.builder().description("description").build())
        );
        assertEquals(error, exception.getMessage());
        verify(itemRequestRepository, times(0)).save(any());
    }

}