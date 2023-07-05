package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.BookingStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateDto {

    private Long id;

    @NotNull
    LocalDateTime start;

    @NotNull
    LocalDateTime end;

    @NotNull
    Long itemId;

    Long bookerId;

    BookingStatus status;

}