package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.validation.GroupValidation;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateDto {

    private Long id;

    @NotNull(groups = {GroupValidation.Create.class})
    LocalDateTime start;

    @NotNull(groups = {GroupValidation.Create.class})
    LocalDateTime end;

    @NotNull(groups = {GroupValidation.Create.class})
    Long itemId;

    Long bookerId;

    BookingStatus status;

}