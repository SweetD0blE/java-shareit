package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {

    Long id;

    String name;

    String description;

    Boolean available;

    Long requestId;
}
