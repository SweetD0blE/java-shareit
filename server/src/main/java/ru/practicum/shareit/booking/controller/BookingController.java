package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable("bookingId") Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsCurrentUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(name = "from", required = false, defaultValue = "0")
                                                   Integer from,
                                                   @RequestParam(name = "size", required = false, defaultValue = "500")
                                                   Integer size) {
        return bookingService.getBookingsCurrentUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsAllItemCurrentUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(name = "from", required = false, defaultValue = "0")
                                                          Integer from,
                                                          @RequestParam(name = "size", required = false, defaultValue = "500")
                                                          Integer size) {
        return bookingService.getBookingsAllItemCurrentUser(userId, state, from, size);
    }

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.createBooking(userId, bookingCreateDto);
    }

    @PatchMapping("{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("bookingId") Long bookingId,
                              @RequestParam("approved") Boolean approve) {
        return bookingService.approveBooking(userId, bookingId, approve);
    }

}