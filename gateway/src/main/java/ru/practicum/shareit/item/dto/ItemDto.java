package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    @NotBlank(groups = {Create.class})
    String name;

    @NotBlank(groups = {Create.class})
    @Size(max = 200, groups = {Create.class})
    String description;

    @NotNull(groups = {Create.class})
    Boolean available;

    Long requestId;
}
