package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateDto {

    Long id;

    @NotNull(groups = {Create.class})
    LocalDateTime start;

    @NotNull(groups = {Create.class})
    LocalDateTime end;

    @NotNull(groups = {Create.class})
    Long itemId;

    Long bookerId;

    BookingStatus status;

}