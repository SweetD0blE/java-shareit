package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "start");
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @InjectMocks
    BookingServiceImpl bookingService;

    private User owner;
    private User booker;

    private User user;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        LocalDateTime start = NOW.minusSeconds(120);
        LocalDateTime end = NOW.minusSeconds(60);
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

        user = User.builder()
                .id(3L)
                .name("name2")
                .email("mail3@mail.ru")
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

        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();
    }

    @Test
    void getBookingsCurrentUserWhenDifferentStateTest() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        PageRequest page = PageRequest.of(0, size, SORT);

        when(bookingRepository.findByBookerId(userId, page)).thenReturn(List.of(booking));
        List<BookingDto> bookingDtos = bookingService.getBookingsCurrentUser(userId, "ALL", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());
        assertEquals(booking.getId(), bookingDtos.get(0).getId());

        when(bookingRepository.findByBookerIdAndStartBeforeAndEndBefore(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "PAST", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        booking.setStart(NOW.plusSeconds(60));

        when(bookingRepository.findByBookerIdAndStartAfter(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "FUTURE", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        booking.setEnd(NOW.plusSeconds(120));

        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "CURRENT", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findByBookerIdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "WAITING", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        booking.setStatus(BookingStatus.REJECTED);

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "REJECTED", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        String error = "Unknown state: ERROR";
        UnsupportedStateException exception = assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsCurrentUser(userId, "ERROR", from, size));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getBookingsAllItemCurrentUserTest() {
        int from = 0;
        int size = 1;
        long userId = owner.getId();
        PageRequest page = PageRequest.of(0, size, SORT);

        when(bookingRepository.findAllByItem_Owner_Id(userId, page)).thenReturn(List.of(booking));

        List<BookingDto> bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "ALL", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        when(bookingRepository.findAllByItem_Owner_IdAndEndIsBefore(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "PAST", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        booking.setStart(NOW.plusSeconds(60));
        when(bookingRepository.findAllByItem_Owner_IdAndStartIsAfter(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "FUTURE", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        booking.setEnd(NOW.plusSeconds(120));
        when(bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "CURRENT", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "WAITING", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "REJECTED", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        String error = "Unknown state: ERROR";
        UnsupportedStateException exception = assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsCurrentUser(userId, "ERROR", from, size));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getBookingByIdWithBooker_ReturnBookingDtoTest() {
        long bookerId = owner.getId();
        long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingDto bookingDto = bookingService.getBookingById(bookerId, bookingId);
        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
    }

    @Test
    void getBookingByIdWhenWrongUser_ReturnObjectNotFoundExceptionTest() {
        long userId = user.getId();
        long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        String error = String.format("Пользователя с id = %d не существует", userId);
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void createBooking_ReturnBookingDtoTest() {
        long bookerId = booker.getId();
        long itemId = item.getId();

        when(userService.getById(bookerId)).thenReturn(booker);
        when(itemService.getById(itemId)).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingOutDto = bookingService.createBooking(bookerId, bookingCreateDto);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void createBookingWhenItemNotAvailable_ReturnObjectNotFoundExceptionTest() {
        item.setAvailable(false);
        long bookerId = booker.getId();
        long itemId = item.getId();
        when(userService.getById(bookerId)).thenReturn(booker);
        when(itemService.getById(itemId)).thenReturn(item);
        String error = String.format("Вещь с id = %d недоступна", itemId);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookerId, bookingCreateDto));
        assertEquals(error, ex.getMessage());
    }

    @Test
    void createBookingWhenFailOwnerItem_ReturnObjectNotFoundExceptionTest() {
        long ownerId = owner.getId();
        long itemId = item.getId();

        when(userService.getById(ownerId)).thenReturn(owner);
        when(itemService.getById(itemId)).thenReturn(item);
        String error = String.format("Вещь с id = %d недоступна для бронирования", item.getId());

        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class, () -> bookingService.createBooking(ownerId, bookingCreateDto));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void approveBooking_ReturnUpdatedBookingDtoTest() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userService.getById(userId)).thenReturn(owner);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDto = bookingService.approveBooking(userId, bookingId, false);

        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
    }

    @Test
    void approveBookingWhenBookingNotFound_ReturnObjectNotFoundExceptionTest() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Бронирование с id = %d не найдено", bookingId);

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void approveBookingWhenNotOwner_ReturnObjectNotFoundExceptionTest() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = "Вы не владеете этой вещью";
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void approveBookingWhenNotAvailable_ReturnValidationExceptionTest() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Бронирование с id = %d недоступно", bookingId);
        when(userService.getById(userId)).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

}