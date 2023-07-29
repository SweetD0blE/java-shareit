package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    List<BookingDto> getBookingsCurrentUser(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getBookingsAllItemCurrentUser(Long userId, String state, Integer from, Integer size);

    BookingDto getBookingById(Long userId, Long bookingId);

    BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approve);
}
