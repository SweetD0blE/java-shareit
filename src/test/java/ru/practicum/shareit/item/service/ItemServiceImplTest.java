package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserService userService;
    @InjectMocks
    ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(120);
        LocalDateTime end = LocalDateTime.now().minusSeconds(60);
        owner = User.builder()
                .id(1L)
                .name("name")
                .email("mail@mail.ru")
                .build();

        booker = User.builder()
                .id(2L)
                .name("name2")
                .email("mail2@mail.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("nameItem")
                .description("description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("text")
                .author(booker)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllItemsByUserId_ReturnEmptyListTest() {
        long userId = booker.getId();
        when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(Collections.emptyList());

        List<ItemOwnerDto> itemDtos = itemService.getAllItemsByUserId(userId, 0, 1);

        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    void getAllItemsByUserId_ReturnListItemsTest() {
        long userId = owner.getId();
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("id"));
        when(itemRepository.findAllByOwnerId(userId, pageRequest)).thenReturn(List.of(item));
        when(bookingRepository.findBookingsByItemIn(List.of(item))).thenReturn(List.of(booking));

        List<ItemOwnerDto> itemOwnerDtos = itemService.getAllItemsByUserId(userId, 0, 1);

        assertNotNull(itemOwnerDtos);
        assertEquals(1, itemOwnerDtos.size());
        assertEquals(booking.getId(), itemOwnerDtos.get(0).getLastBooking().getId());
    }

    @Test
    void getItemById_ReturnItemTest() {
        long ownerId = owner.getId();
        long itemId = item.getId();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingsByItem_Id(itemId)).thenReturn(List.of(booking));
        when(commentRepository.findCommentsByItem_Id(itemId)).thenReturn(List.of(comment));

        ItemOwnerDto itemOwnerDto = itemService.getItemById(ownerId, itemId);

        assertNotNull(itemOwnerDto);
        assertEquals(itemId, itemOwnerDto.getId());
        assertEquals(comment.getId(), itemOwnerDto.getComments().get(0).getId());
    }

    @Test
    void getSearchItem_ReturnListItemsTest() {
        when(itemRepository.search(any(), any())).thenReturn(List.of(item));

        List<ItemDto> itemDtos = itemService.getSearchItem("nameItem", 0, 1);

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(item.getId(), itemDtos.get(0).getId());
    }

    @Test
    void getSearchItem_ReturnEmptyListTest() {
        List<ItemDto> itemDtos = itemService.getSearchItem("", 0, 1);

        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    void saveItem_ReturnSavedItemDtoTest() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto saveItemDto = ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        ItemDto itemDto = itemService.createItem(userId, saveItemDto);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItem_ReturnItemDtoTest() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userService.getById(userId)).thenReturn(owner);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        String newName = "nameUpdate";
        String newDescription = "newDescription";
        item.setName(newName);
        item.setDescription(newDescription);
        when(itemRepository.save(any())).thenReturn(item);
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .name(newName)
                .description(newDescription)
                .build();
        ItemDto itemDto = itemService.updateItem(userId, itemId, itemDtoToUpdate);
        assertNotNull(itemDto);
        assertEquals("nameUpdate", itemDto.getName());
    }

    @Test
    void createComment_ReturnCommentDtoTest() {
        long userId = booker.getId();
        long itemId = item.getId();
        when(userService.getById(userId)).thenReturn(booker);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findBookingByItem_IdAndStatusOrderByEndAsc(itemId, BookingStatus.APPROVED))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentDto commentDto = CommentDto.builder().text("text").build();

        CommentDto commentDtoOut = itemService.createComment(userId, itemId, commentDto);

        assertNotNull(commentDtoOut);
        assertEquals(comment.getId(), commentDtoOut.getId());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createCommentWhenNotBookingCompleted_ReturnValidationExceptionTest() {
        long itemId = item.getId();
        long ownerId = owner.getId();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findBookingByItem_IdAndStatusOrderByEndAsc(anyLong(), any()))
                .thenReturn(Collections.emptyList());
        String error = "Вы можете добавить комментарий только после завершения бронирования.";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.createComment(ownerId, itemId, CommentDto.builder().text("text").build()));

        assertEquals(error, exception.getMessage());
    }
}