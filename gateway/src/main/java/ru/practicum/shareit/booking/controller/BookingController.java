package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingClient bookingClient;

    @GetMapping(value = "{bookingId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                 @PathVariable("bookingId") @Positive long bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping(value = "/owner", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBookingsAllItemCurrentUser(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                                @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                                @RequestParam(name = "from", required = false, defaultValue = "0")
                                                                @PositiveOrZero Integer from,
                                                                @RequestParam(name = "size", required = false, defaultValue = "500")
                                                                @Positive Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsAllItem(userId, state, from, size);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                @Validated(Create.class) @RequestBody BookingCreateDto bookingCreateDto) {
        if (!(bookingCreateDto.getStart().isAfter(LocalDateTime.now()) &&
                bookingCreateDto.getEnd().isAfter(LocalDateTime.now()) &&
                bookingCreateDto.getStart().isBefore(bookingCreateDto.getEnd()))) {
            throw new ValidationException("Date is not correct");
        }
        return bookingClient.addBooking(userId, bookingCreateDto);
    }

    @PatchMapping(value = "{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                 @PathVariable("bookingId") @Positive long bookingId,
                                                 @RequestParam("approved") Boolean approved) {
        return bookingClient.approve(userId, bookingId, approved);
    }

}