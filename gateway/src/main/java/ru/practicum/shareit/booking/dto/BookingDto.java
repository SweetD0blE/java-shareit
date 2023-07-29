package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.GroupValidation;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {

    Long id;

    @NotNull(groups = {GroupValidation.Create.class})
    LocalDateTime start;

    @NotNull(groups = {GroupValidation.Create.class})
    LocalDateTime end;

    ItemDto item;

    UserDto booker;

    BookingStatus status;
}
