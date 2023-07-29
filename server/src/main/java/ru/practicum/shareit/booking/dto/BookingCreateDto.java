package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateDto {

    private Long id;

    LocalDateTime start;

    LocalDateTime end;

    Long itemId;

    Long bookerId;

    BookingStatus status;

}