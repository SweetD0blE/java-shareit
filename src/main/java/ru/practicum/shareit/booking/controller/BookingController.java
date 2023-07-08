package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("{bookingId}")
    public BookingDto getBookingById(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                     @PathVariable("bookingId") @Positive Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsCurrentUser(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(name = "from", required = false, defaultValue = "0")
                                                   @PositiveOrZero Integer from,
                                                   @RequestParam(name = "size", required = false, defaultValue = "500")
                                                   @Positive Integer size) {
        return bookingService.getBookingsCurrentUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsAllItemCurrentUser(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                          @RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(name = "from", required = false, defaultValue = "0")
                                                          @PositiveOrZero Integer from,
                                                          @RequestParam(name = "size", required = false, defaultValue = "500")
                                                          @Positive Integer size) {
        return bookingService.getBookingsAllItemCurrentUser(userId, state, from, size);
    }

    @PostMapping
    public BookingDto createBooking(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                    @Validated(Create.class) @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.createBooking(userId, bookingCreateDto);
    }

    @PatchMapping("{bookingId}")
    public BookingDto approve(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @PathVariable("bookingId") @Positive Long bookingId,
                              @RequestParam("approved") Boolean approve) {
        return bookingService.approveBooking(userId, bookingId, approve);
    }

}