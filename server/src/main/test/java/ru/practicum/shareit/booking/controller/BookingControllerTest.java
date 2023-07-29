package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final BookingService bookingService;

    String url = "/bookings";

    UserDto.UserDtoBuilder userDtoBuilder;
    ItemDto.ItemDtoBuilder itemDtoBuilder;
    BookingCreateDto.BookingCreateDtoBuilder bookingCreateDtoBuilder;
    BookingDto.BookingDtoBuilder bookingDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        userDtoBuilder = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@email.ru");
        itemDtoBuilder = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true);
        bookingCreateDtoBuilder = BookingCreateDto.builder()
                .itemId(1L)
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2));
        bookingDtoBuilder = BookingDto.builder()
                .id(1L)
                .booker(userDtoBuilder.build())
                .item(itemDtoBuilder.build())
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2))
                .status(BookingStatus.WAITING);
    }

    @Test
    void shouldGetBookingById_ReturnStatus200AndCorrectJson() throws Exception {
        BookingDto bookingDto = BookingDto.builder().build();
        String json = mapper.writeValueAsString(bookingDto);

        when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingDto);
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void shouldGetBookingByIdWhenNotExistingUserId_ReturnStatus404() throws Exception {
        when(bookingService.getBookingById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id = %d не был найден", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Пользователь с id = 999 не был найден\"}"));
    }

    @Test
    void shouldGetBookingByIdWhenNotExistingBookingId_ReturnStatus404() throws Exception {
        when(bookingService.getBookingById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Бронирование с id = %d не найдено", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Бронирование с id = 999 не найдено\"}"));
    }

    @Test
    void shouldGetBookingsCurrentUser_ReturnEmptyList() throws Exception {
        when(bookingService.getBookingsCurrentUser(1L, "REJECTED", 0, 10))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetBookingsCurrentUser_ReturnListBookings() throws Exception {
        BookingDto bookingDto = bookingDtoBuilder.build();
        when(bookingService.getBookingsCurrentUser(1L, "WAITING", 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldGetBookingsCurrentUserIfStateFail_ReturnStatus400() throws Exception {
        when(bookingService.getBookingsCurrentUser(1L, "FAIL", 0, 10))
                .thenThrow(new UnsupportedStateException(String.format("Unknown state: %s", "FAIL")));
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "FAIL")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"Unknown state: FAIL\"}"));
    }

    @Test
    void shouldGetBookingsAllItemCurrentUser_ReturnEmptyList() throws Exception {
        when(bookingService.getBookingsAllItemCurrentUser(1L, "REJECTED", 0, 10))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get(url + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetBookingsAllItemCurrentUser_ReturnListBookings() throws Exception {
        BookingDto bookingDto = bookingDtoBuilder.build();
        when(bookingService.getBookingsAllItemCurrentUser(1L, "WAITING", 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(get(url + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldGetBookingsAllItemCurrentUserIfStateFail_ReturnStatus400() throws Exception {
        when(bookingService.getBookingsAllItemCurrentUser(1L, "FAIL", 0, 10))
                .thenThrow(new UnsupportedStateException(String.format("Unknown state: %s", "FAIL")));
        mockMvc.perform(get(url + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "FAIL")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"Unknown state: FAIL\"}"));
    }

    @Test
    void createBooking_ReturnStatus200Test() throws Exception {
        BookingCreateDto bookingCreateDto = bookingCreateDtoBuilder.build();
        BookingDto bookingDto = bookingDtoBuilder.build();
        String json = mapper.writeValueAsString(bookingCreateDto);
        String jsonAdded = mapper.writeValueAsString(bookingDto);

        when(bookingService.createBooking(1L, bookingCreateDto)).thenReturn(bookingDto);

        mockMvc.perform(post(url)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void approveIfTrue_ReturnStatus200AndCorrectJsonTest() throws Exception {
        BookingDto bookingDto = bookingDtoBuilder.status(BookingStatus.APPROVED).build();
        String json = mapper.writeValueAsString(bookingDto);

        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(bookingDto);

        mockMvc.perform(patch(url + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void approveIfFalse_ReturnStatus200AndCorrectJsonTest() throws Exception {
        BookingDto bookingDto = bookingDtoBuilder.status(BookingStatus.REJECTED).build();
        String json = mapper.writeValueAsString(bookingDto);

        when(bookingService.approveBooking(1L, 1L, false)).thenReturn(bookingDto);

        mockMvc.perform(patch(url + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void approveWithNotFoundUser_ReturnStatus404Test() throws Exception {
        when(bookingService.approveBooking(999L, 1L, true))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/1")
                        .header("X-Sharer-User-Id", 999)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void approveWithNotFoundBooking_ReturnStatus404Test() throws Exception {
        when(bookingService.approveBooking(1L, 999L, true))
                .thenThrow(new ObjectNotFoundException(String.format("Booking not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/999")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Booking not found: id=999\"}"));
    }

    @Test
    void approveWithStatusNotWaiting_ReturnStatus404Test() throws Exception {
        when(bookingService.approveBooking(1L, 1L, false))
                .thenThrow(new ValidationException(String.format("Booking not available: id=%d", 1L)));
        mockMvc.perform(patch(url + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"Booking not available: id=1\"}"));
    }
}